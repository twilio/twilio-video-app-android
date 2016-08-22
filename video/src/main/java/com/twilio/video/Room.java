package com.twilio.video;

import android.os.Handler;

import com.twilio.video.internal.Logger;

import java.util.HashMap;
import java.util.Map;

public class Room {

    public interface Listener {
        void onConnected(Room room);

        void onConnectFailure(VideoException error);

        void onDisconnected(Room room, VideoException error);

        void onParticipantConnected(Room room, Participant participant);

        void onParticipantDisconnected(Room room, Participant participant);

    }

    private static final Logger logger = Logger.getLogger(Room.class);
    private long nativeRoomContext;
    private final String name;
    private final LocalMedia localMedia;
    private String sid;
    private RoomState roomState;
    private Map<String, Participant> participantMap = new HashMap<>();
    private InternalRoomListenerHandle internalRoomListenerHandle;
    private InternalRoomListenerImpl internalRoomListenerImpl;
    private Room.Listener listener;
    private final Handler handler;

    Room(String name, LocalMedia localMedia, Room.Listener listener, Handler handler) {
        this.name = name;
        this.localMedia = localMedia;
        this.sid = "";
        this.roomState = RoomState.DISCONNECTED;
        this.listener = listener;
        this.internalRoomListenerImpl = new InternalRoomListenerImpl();
        this.internalRoomListenerHandle = new InternalRoomListenerHandle(internalRoomListenerImpl);
        this.handler = handler;
    }

    public String getName() {
        return name;
    }

    public String getSid() {
        return sid;
    }

    public RoomState getState() {
        return roomState;
    }

    public Map<String, Participant> getParticipants() {
        return new HashMap<>(participantMap);
    }

    public LocalMedia getLocalMedia() {
        return localMedia;
    }

    public synchronized void disconnect() {
        if (roomState != RoomState.DISCONNECTED && nativeRoomContext != 0) {
            nativeDisconnect(nativeRoomContext);
        }
    }

    long getListenerNativeHandle() {
        return internalRoomListenerHandle.get();
    }

    void setNativeContext(long nativeRoomHandle) {
        this.nativeRoomContext = nativeRoomHandle;
    }

    /*
     * Needed for synchronizing during room creation.
     */
    Object getConnectLock() {
        return internalRoomListenerImpl;
    }

    synchronized void release() {
        if (nativeRoomContext != 0) {
            nativeRelease(nativeRoomContext);
            nativeRoomContext = 0;
            // TODO: Once native video team makes decision about participant strategy
            // after disconnect, make sure it is properly implemented here. For now we are just
            // removing native participant context in order to prevent memory leak.
            for (Participant participant : participantMap.values()) {
                participant.release();
            }
            internalRoomListenerHandle.release();
        }
    }

    void setState(RoomState newRoomState) {
        roomState = newRoomState;
    }

    void setSid(String roomSid) {
        this.sid = roomSid;
    }

    // JNI Callbacks Interface
    interface InternalRoomListener {
        void onConnected(String roomSid);
        void onDisconnected(int errorCode);
        void onConnectFailure(int errorCode);
        void onParticipantConnected(Participant participant);
        void onParticipantDisconnected(String participantSid);
    }

    class InternalRoomListenerImpl implements InternalRoomListener {

        @Override
        public synchronized void onConnected(String roomSid) {
            logger.d("onConnected()");
            setState(RoomState.CONNECTED);
            setSid(roomSid);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onConnected(Room.this);
                }
            });
        }

        @Override
        public synchronized void onDisconnected(final int errorCode) {
            logger.d("onDisconnected()");
            setState(RoomState.DISCONNECTED);
            release();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onDisconnected(Room.this, new VideoException(errorCode, ""));
                }
            });
        }

        @Override
        public synchronized void onConnectFailure(final int errorCode) {
            logger.d("onConnectFailure()");
            setState(RoomState.DISCONNECTED);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Room.this.listener.onConnectFailure(new VideoException(errorCode, ""));
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

    }

    class InternalRoomListenerHandle extends NativeHandle {


        public InternalRoomListenerHandle(InternalRoomListener listener) {
            super(listener);
        }

        /*
         * Native Handle
         */
        @Override
        protected native long nativeCreate(Object object);

        @Override
        protected native void nativeFree(long nativeHandle);


    }

    private native void nativeDisconnect(long nativeRoomContext);
    private native void nativeRelease(long nativeRoomContext);
}