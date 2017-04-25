package com.twilio.video;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A Room represents a media session with zero or more remote Participants. Media shared by any one
 * {@link Participant} is distributed equally to all other Participants.
 */
public class Room {
    private static final Logger logger = Logger.getLogger(Room.class);

    private long nativeRoomContext;
    private Context context;
    private String name;
    private String sid;
    private RoomState roomState;
    private Map<String, Participant> participantMap = new HashMap<>();
    private InternalRoomListenerHandle internalRoomListenerHandle;
    private InternalRoomListenerImpl internalRoomListenerImpl;
    private InternalStatsListenerHandle internalStatsListenerHandle;
    private InternalStatsListenerImpl internalStatsListenerImpl;
    private LocalParticipant localParticipant;
    private final Room.Listener listener;
    private final Handler handler;
    private Queue<Pair<Handler, StatsListener>> statsListenersQueue;
    private ConnectOptions cachedConnectOptions;
    private MediaFactory mediaFactory;

    Room(Context context, String name, Handler handler, Listener listener) {
        this.context = context;
        this.name = name;
        this.sid = "";
        this.roomState = RoomState.DISCONNECTED;
        this.listener = listener;
        this.internalRoomListenerImpl = new InternalRoomListenerImpl();
        this.internalRoomListenerHandle = new InternalRoomListenerHandle(internalRoomListenerImpl);
        this.handler = handler;
        this.statsListenersQueue = new ConcurrentLinkedQueue<>();
        this.mediaFactory = MediaFactory.instance(context);
    }

    /**
     * Returns the name of the current room. This method will return the SID if the room was
     * created without a name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the SID of the current room.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Returns the current room state.
     */
    public RoomState getState() {
        return roomState;
    }

    /**
     * Returns whether any media in the Room is being recorded.
     */
    public boolean isRecording() {
        return roomState == RoomState.CONNECTED ?
                nativeIsRecording(nativeRoomContext) : false;
    }

    /**
     * Returns all currently connected participants.
     *
     * @return a map of identities to participants.
     */
    public Map<String, Participant> getParticipants() {
        return new HashMap<>(participantMap);
    }

    /**
     * Returns the current local participant. If the room has not reached
     * {@link RoomState#CONNECTED} then this method will return null.
     */
    public LocalParticipant getLocalParticipant() {
        return localParticipant;
    }

    /**
     * Retrieve stats for all media tracks and notify {@link StatsListener} via calling thread.
     * In case where room is in {@link RoomState#DISCONNECTED} state, reports won't be delivered.
     *
     * @param statsListener listener that receives stats reports for all media tracks.
     */
    public synchronized void getStats(@NonNull StatsListener statsListener) {
        if (statsListener == null) {
            throw new NullPointerException("StatsListener must not be null");
        }
        if (roomState == RoomState.DISCONNECTED) {
            return;
        }
        if (internalStatsListenerImpl == null) {
            internalStatsListenerImpl = new InternalStatsListenerImpl();
            internalStatsListenerHandle =
                    new InternalStatsListenerHandle(internalStatsListenerImpl);
        }
        statsListenersQueue.offer(
                new Pair<Handler, StatsListener>(Util.createCallbackHandler(), statsListener));
        nativeGetStats(nativeRoomContext, internalStatsListenerHandle.get());
    }

    /**
     * Disconnects from the room.
     */
    public synchronized void disconnect() {
        if (roomState != RoomState.DISCONNECTED && nativeRoomContext != 0) {
            if (localParticipant != null) {
                localParticipant.release();
            }
            nativeDisconnect(nativeRoomContext);
        }
    }

    long getListenerNativeHandle() {
        return internalRoomListenerHandle.get();
    }

    void setNativeContext(long nativeRoomHandle) {
        this.nativeRoomContext = nativeRoomHandle;
    }

    void onNetworkChanged(Video.NetworkChangeEvent networkChangeEvent) {
        if (nativeRoomContext != 0) {
            nativeOnNetworkChange(nativeRoomContext, networkChangeEvent);
        }
    }

    /*
     * Needed for synchronizing during room creation.
     */
    Object getConnectLock() {
        return internalRoomListenerImpl;
    }

    /*
     * We need to synchronize access to room listener during initialization and make
     * sure that onConnect() callback won't get called before connect() exits and Room
     * creation is fully completed.
     */
    void connect(ConnectOptions connectOptions) {
        synchronized (internalRoomListenerImpl) {
            // Retain the connect options to provide the audio and video tracks upon connect
            cachedConnectOptions = connectOptions;
            nativeRoomContext = nativeConnect(
                connectOptions,
                MediaFactory.instance(context).getNativeMediaFactoryHandle(),
                internalRoomListenerHandle.get());
            mediaFactory.addRef();
            roomState = RoomState.CONNECTING;
        }
    }

    // Doesn't release native room observer
    synchronized void release() {
        if (nativeRoomContext != 0) {
            mediaFactory.release();
            for (Participant participant : participantMap.values()) {
                participant.release();
            }
            nativeRelease(nativeRoomContext);
            nativeRoomContext = 0;
            internalRoomListenerHandle.release();
            internalRoomListenerHandle = null;
            if (internalStatsListenerHandle != null) {
                internalStatsListenerHandle.release();
                internalStatsListenerHandle = null;
            }
            cleanupStatsListenerQueue();
        }
    }

    private void cleanupStatsListenerQueue() {
        for (final Pair<Handler, StatsListener> listenerPair : statsListenersQueue) {
            listenerPair.first.post(new Runnable() {
                @Override
                public void run() {
                    listenerPair.second.onStats(new ArrayList<StatsReport>());
                }
            });
        }
        statsListenersQueue.clear();
    }

    void setState(RoomState roomState) {
        this.roomState = roomState;
    }

    // JNI Callbacks Interface
    interface InternalRoomListener {
        void onConnected(String roomSid,
                         long localParticipantHandle,
                         String localParticipantSid,
                         String localParticipantIdentity,
                         List<Participant> participantList);
        void onDisconnected(TwilioException twilioException);
        void onConnectFailure(TwilioException twilioException);
        void onParticipantConnected(Participant participant);
        void onParticipantDisconnected(String participantSid);
        void onRecordingStarted();
        void onRecordingStopped();
    }

    class InternalRoomListenerImpl implements InternalRoomListener {

        // TODO: find better way to pass Handler to Media
        Handler getHandler() {
            return Room.this.handler;
        }

        @Override
        public synchronized void onConnected(String roomSid,
                                             long nativeLocalParticipantHandle,
                                             String localParticipantSid,
                                             String localParticipantIdentity,
                                             List<Participant> participantList) {
            logger.d("onConnected()");

            Room.this.sid = roomSid;
            if (Room.this.name == null || Room.this.name.isEmpty()) {
                Room.this.name = roomSid;
            }
            Room.this.localParticipant = new LocalParticipant(
                nativeLocalParticipantHandle, localParticipantSid, localParticipantIdentity, cachedConnectOptions.getAudioTracks(), cachedConnectOptions.getVideoTracks());
            cachedConnectOptions = null;
            for (Participant participant : participantList) {
                participantMap.put(participant.getSid(), participant);
            }
            Room.this.roomState = RoomState.CONNECTED;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onConnected(Room.this);
                }
            });
        }

        @Override
        public synchronized void onDisconnected(final TwilioException twilioException) {
            logger.d("onDisconnected()");
            /*
             * Release all Android level Room resources in notifier thread and before invoking
             * onDisconnected callback for the following reasons:
             *
             * 1. Ensures that native WebRTC tracks are not null when removing remote renderers.
             * 2. Protects developers from potentially referencing dead WebRTC tracks in
             *    onDisconnected callback.
             *
             * See GSDK-1007, GSDK-1079, and GSDK-1043 for more details.
             */
            release();

            if (localParticipant != null) {
                // Ensure the local participant is released if the disconnect was issued by the core
                localParticipant.release();
            }
            Room.this.roomState = RoomState.DISCONNECTED;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onDisconnected(Room.this, twilioException);
                }
            });
        }

        @Override
        public synchronized void onConnectFailure(final TwilioException twilioException) {
            logger.d("onConnectFailure()");
            release();
            Room.this.roomState = RoomState.DISCONNECTED;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onConnectFailure(Room.this, twilioException);
                }
            });
        }

        @Override
        public synchronized void onParticipantConnected(final Participant participant) {
            logger.d("onParticipantConnected()");

            participantMap.put(participant.getSid(), participant);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onParticipantConnected(Room.this, participant);
                }
            });
        }

        @Override
        public synchronized void onParticipantDisconnected(String participantSid) {
            logger.d("onParticipantDisconnected()");

            final Participant participant = participantMap.remove(participantSid);
            if (participant == null) {
                logger.w("Received participant disconnected callback for non-existent participant");
                return;
            }
            participant.release();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onParticipantDisconnected(Room.this, participant);
                }
            });
        }

        @Override
        public void onRecordingStarted() {
            logger.d("onRecordingStarted()");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onRecordingStarted(Room.this);
                }
            });
        }

        @Override
        public void onRecordingStopped() {
            logger.d("onRecordingStopped()");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onRecordingStopped(Room.this);
                }
            });
        }

    }

    /**
     * Listener definition of room related events.
     */
    public interface Listener {
        /**
         * Called when a room has succeeded.
         *
         * @param room the connected room.
         */
        void onConnected(Room room);

        /**
         * Called when a connection to a room failed.
         *
         * @param room the room that failed to be connected to.
         * @param twilioException an exception describing why connect failed.
         */
        void onConnectFailure(Room room, TwilioException twilioException);

        /**
         * Called when a room has been disconnected from.
         *
         * @param room the room that was disconnected from.
         * @param twilioException An exception if there was a problem that caused the room to be
         *              disconnected from. This value will be null is there were no problems
         *              disconnecting from the room.
         */
        void onDisconnected(Room room, TwilioException twilioException);

        /**
         * Called when a participant has connected to a room.
         *
         * @param room the room the participant connected to.
         * @param participant the newly connected participant.
         */
        void onParticipantConnected(Room room, Participant participant);

        /**
         * Called when a participant has disconnected from a room.
         * @param room the room the participant disconnected from.
         * @param participant the disconnected participant.
         */
        void onParticipantDisconnected(Room room, Participant participant);

        /**
         * Called when the media being shared to a {@link Room} is being recorded.
         * @param room
         */
        void onRecordingStarted(Room room);

        /**
         * Called when the media being shared to a {@link Room} is no longer being recorded.
         * @param room
         */
        void onRecordingStopped(Room room);
    }

    class InternalRoomListenerHandle extends NativeHandle {
        InternalRoomListenerHandle(InternalRoomListener listener) {
            super(listener);
        }

        /*
         * Native Handle
         */
        @Override
        protected native long nativeCreate(Object object);

        @Override
        protected native void nativeRelease(long nativeHandle);
    }

    // JNI Callbacks Interface
    interface InternalStatsListener {
        void onStats(List<StatsReport> statsReports);
    }

    class InternalStatsListenerImpl implements InternalStatsListener {

        public void onStats(final List<StatsReport> statsReports) {
            final Pair<Handler, StatsListener> statsPair = Room.this.statsListenersQueue.poll();
            if (statsPair != null) {
                statsPair.first.post(new Runnable() {
                    @Override
                    public void run() {
                        statsPair.second.onStats(statsReports);
                    }
                });
            }
        }
    }

    class InternalStatsListenerHandle extends NativeHandle {

        public InternalStatsListenerHandle(InternalStatsListener listener) {
            super(listener);
        }

        /*
         * Native Handle
         */
        @Override
        protected native long nativeCreate(Object object);

        @Override
        protected native void nativeRelease(long nativeHandle);
    }

    private native long nativeConnect(ConnectOptions ConnectOptions,
                                      long nativeMediaFactoryHandle,
                                      long nativeRoomListenerHandle);
    private native boolean nativeIsRecording(long nativeRoomContext);
    private native void nativeDisconnect(long nativeRoomContext);
    private native void nativeGetStats(long nativeRoomContext, long nativeStatsObserver);
    private native void nativeRelease(long nativeRoomContext);
    private native void nativeOnNetworkChange(long nativeRoomContext,
                                              Video.NetworkChangeEvent networkChangeEvent);
}
