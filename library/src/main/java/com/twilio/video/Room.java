/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.annotation.SuppressLint;
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
 * {@link RemoteParticipant} is distributed equally to all other Participants.
 */
public class Room {
    private static final Logger logger = Logger.getLogger(Room.class);

    private long nativeRoomDelegate;
    private Context context;
    private String name;
    private String sid;
    private RoomState roomState;
    private Map<String, RemoteParticipant> participantMap = new HashMap<>();
    private LocalParticipant localParticipant;
    private final Room.Listener listener;
    private final Handler handler;
    private Queue<Pair<Handler, StatsListener>> statsListenersQueue;
    private MediaFactory mediaFactory;

    /*
     * The contract for Room JNI callbacks is as follows:
     *
     * 1. All event callbacks are done on the same thread the developer used to connect to a room.
     * 2. Create and release all native memory on the same thread. In the case of a Room, the
     * RoomDelegate is created and released on the developer thread and the native room, room
     * observer, local participant, and participants are created and released on notifier thread.
     * 3. All Room fields must be mutated on the developer's thread.
     *
     * Not abiding by this contract, may result in difficult to debug JNI crashes,
     * incorrect return values in the synchronous API methods, or missed callbacks.
     */
    private final Room.Listener roomListenerProxy =
            new Room.Listener() {
                @Override
                public void onConnected(final Room room) {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onConnected()");
                                    Room.this.listener.onConnected(room);
                                }
                            });
                }

                @Override
                public void onConnectFailure(
                        final Room room, final TwilioException twilioException) {
                    // Release native room
                    releaseRoom();

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onConnectFailure()");

                                    // Update room state
                                    Room.this.roomState = RoomState.DISCONNECTED;

                                    // Release native room delegate
                                    release();

                                    // Notify developer
                                    Room.this.listener.onConnectFailure(room, twilioException);
                                }
                            });
                }

                @Override
                public void onDisconnected(final Room room, final TwilioException twilioException) {
                    // Release native room
                    releaseRoom();

                    // Ensure the local participant is released if the disconnect was issued by the
                    // core
                    if (localParticipant != null) {
                        localParticipant.release();
                    }

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onDisconnected()");

                                    // Update room state
                                    Room.this.roomState = RoomState.DISCONNECTED;

                                    // Release native room delegate
                                    release();

                                    // Notify developer
                                    Room.this.listener.onDisconnected(room, twilioException);
                                }
                            });
                }

                @Override
                public void onParticipantConnected(
                        final Room room, final RemoteParticipant participant) {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onParticipantConnected()");

                                    // Update participants
                                    participantMap.put(participant.getSid(), participant);

                                    // Notify developer
                                    Room.this.listener.onParticipantConnected(room, participant);
                                }
                            });
                }

                @Override
                public void onParticipantDisconnected(
                        final Room room, final RemoteParticipant remoteParticipant) {
                    // Release participant
                    remoteParticipant.release();

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onParticipantDisconnected()");

                                    // Update participants
                                    participantMap.remove(remoteParticipant.getSid());

                                    // Notify developer
                                    Room.this.listener.onParticipantDisconnected(
                                            room, remoteParticipant);
                                }
                            });
                }

                @Override
                public void onRecordingStarted(final Room room) {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onRecordingStarted()");
                                    Room.this.listener.onRecordingStarted(room);
                                }
                            });
                }

                @Override
                public void onRecordingStopped(final Room room) {
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    ThreadChecker.checkIsValidThread(handler);
                                    logger.d("onRecordingStopped()");
                                    Room.this.listener.onRecordingStopped(room);
                                }
                            });
                }
            };
    private final StatsListener statsListenerProxy =
            new StatsListener() {
                @Override
                public void onStats(final List<StatsReport> statsReports) {
                    final Pair<Handler, StatsListener> statsPair =
                            Room.this.statsListenersQueue.poll();
                    if (statsPair != null) {
                        statsPair.first.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        statsPair.second.onStats(statsReports);
                                    }
                                });
                    }
                }
            };

    Room(Context context, String name, Handler handler, Listener listener) {
        this.context = context;
        this.name = name;
        this.sid = "";
        this.roomState = RoomState.DISCONNECTED;
        this.listener = listener;
        this.handler = handler;
        this.statsListenersQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Returns the name of the current room. This method will return the SID if the room was created
     * without a name.
     */
    public String getName() {
        return name;
    }

    /** Returns the SID of the current room. */
    public String getSid() {
        return sid;
    }

    /** Returns the current room state. */
    public synchronized RoomState getState() {
        return roomState;
    }

    /** Returns whether any media in the Room is being recorded. */
    public synchronized boolean isRecording() {
        return roomState == RoomState.CONNECTED && nativeIsRecording(nativeRoomDelegate);
    }

    /**
     * Returns all currently connected participants.
     *
     * @return list of participants.
     */
    public synchronized List<RemoteParticipant> getRemoteParticipants() {
        return new ArrayList<>(participantMap.values());
    }

    /**
     * Returns the current local participant. If the room has not reached {@link
     * RoomState#CONNECTED} then this method will return null.
     */
    public synchronized LocalParticipant getLocalParticipant() {
        return localParticipant;
    }

    /**
     * Retrieve stats for all media tracks and notify {@link StatsListener} via calling thread. In
     * case where room is in {@link RoomState#DISCONNECTED} state, reports won't be delivered.
     *
     * @param statsListener listener that receives stats reports for all media tracks.
     */
    public synchronized void getStats(@NonNull StatsListener statsListener) {
        Preconditions.checkNotNull(statsListener, "StatsListener must not be null");
        if (roomState == RoomState.DISCONNECTED) {
            return;
        }
        statsListenersQueue.offer(new Pair<>(Util.createCallbackHandler(), statsListener));
        nativeGetStats(nativeRoomDelegate);
    }

    /** Disconnects from the room. */
    public synchronized void disconnect() {
        if (roomState != RoomState.DISCONNECTED && nativeRoomDelegate != 0) {
            if (localParticipant != null) {
                localParticipant.release();
            }
            nativeDisconnect(nativeRoomDelegate);
        }
    }

    void onNetworkChanged(Video.NetworkChangeEvent networkChangeEvent) {
        if (nativeRoomDelegate != 0) {
            nativeOnNetworkChange(nativeRoomDelegate, networkChangeEvent);
        }
    }

    /*
     * We need to synchronize access to room listener during initialization and make
     * sure that onConnect() callback won't get called before connect() exits and Room
     * creation is fully completed.
     */
    @SuppressLint("RestrictedApi")
    void connect(final ConnectOptions connectOptions) {
        // Check if audio or video tracks have been released
        ConnectOptions.checkAudioTracksReleased(connectOptions.getAudioTracks());
        ConnectOptions.checkVideoTracksReleased(connectOptions.getVideoTracks());

        synchronized (roomListenerProxy) {
            /*
             * Tests are allowed to provide a test MediaFactory to simulate media scenarios on the
             * same device.
             */
            mediaFactory =
                    (connectOptions.getMediaFactory() == null)
                            ? MediaFactory.instance(this, context)
                            : connectOptions.getMediaFactory();
            nativeRoomDelegate =
                    nativeConnect(
                            connectOptions,
                            roomListenerProxy,
                            statsListenerProxy,
                            mediaFactory.getNativeMediaFactoryHandle(),
                            handler);
            roomState = RoomState.CONNECTING;
        }
    }

    /*
     * Called by JNI layer to finalize Room state after connected.
     */
    @SuppressWarnings("unused")
    private synchronized void setConnected(
            String roomSid,
            LocalParticipant localParticipant,
            List<RemoteParticipant> remoteParticipants) {
        logger.d("setConnected()");
        this.sid = roomSid;
        if (this.name == null || this.name.isEmpty()) {
            this.name = roomSid;
        }
        this.localParticipant = localParticipant;
        for (RemoteParticipant remoteParticipant : remoteParticipants) {
            participantMap.put(remoteParticipant.getSid(), remoteParticipant);
        }
        this.roomState = RoomState.CONNECTED;
    }

    /*
     * Release all native Room memory in notifier thread and before invoking
     * onDisconnected callback for the following reasons:
     *
     * 1. Ensures that native WebRTC tracks are not null when removing remote renderers.
     * 2. Protects developers from potentially referencing dead WebRTC tracks in
     *    onDisconnected callback.
     *
     * See GSDK-1007, GSDK-1079, and GSDK-1043 for more details.
     *
     * Thread validation is performed in RoomDelegate.
     */
    private synchronized void releaseRoom() {
        if (nativeRoomDelegate != 0) {
            for (RemoteParticipant remoteParticipant : participantMap.values()) {
                remoteParticipant.release();
            }
            nativeReleaseRoom(nativeRoomDelegate);
            cleanupStatsListenerQueue();
        }
    }

    /*
     * Release the native RoomDelegate from developer thread once the native Room memory
     * has been released.
     */
    private synchronized void release() {
        ThreadChecker.checkIsValidThread(handler);

        if (nativeRoomDelegate != 0) {
            nativeRelease(nativeRoomDelegate);
            nativeRoomDelegate = 0;
            mediaFactory.release(this);
        }
    }

    private void cleanupStatsListenerQueue() {
        for (final Pair<Handler, StatsListener> listenerPair : statsListenersQueue) {
            listenerPair.first.post(
                    new Runnable() {
                        @Override
                        public void run() {
                            listenerPair.second.onStats(new ArrayList<StatsReport>());
                        }
                    });
        }
        statsListenersQueue.clear();
    }

    /** Listener definition of room related events. */
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
         *     disconnected from. This value will be null is there were no problems disconnecting
         *     from the room.
         */
        void onDisconnected(Room room, TwilioException twilioException);

        /**
         * Called when a participant has connected to a room.
         *
         * @param room the room the participant connected to.
         * @param remoteParticipant the newly connected participant.
         */
        void onParticipantConnected(Room room, RemoteParticipant remoteParticipant);

        /**
         * Called when a participant has disconnected from a room. The disconnected participant's
         * audio and video tracks will still be available in their last known state. Video tracks
         * renderers are removed when a participant is disconnected.
         *
         * @param room the room the participant disconnected from.
         * @param remoteParticipant the disconnected participant.
         */
        void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant);

        /**
         * This method is only called when a {@link Room} which was not previously recording starts
         * recording. If you've joined a {@link Room} which is already recording this event will not
         * be fired.
         *
         * @param room
         */
        void onRecordingStarted(Room room);

        /**
         * This method is only called when a {@link Room} which was previously recording stops
         * recording. If you've joined a {@link Room} which is not recording this event will not be
         * fired.
         *
         * @param room
         */
        void onRecordingStopped(Room room);
    }

    private native long nativeConnect(
            ConnectOptions ConnectOptions,
            Listener listenerProxy,
            StatsListener statsListenerProxy,
            long nativeMediaFactoryHandle,
            Handler handler);

    private native boolean nativeIsRecording(long nativeRoomDelegate);

    private native void nativeGetStats(long nativeRoomDelegate);

    private native void nativeOnNetworkChange(
            long nativeRoomDelegate, Video.NetworkChangeEvent networkChangeEvent);

    private native void nativeDisconnect(long nativeRoomDelegate);

    private native void nativeReleaseRoom(long nativeRoomDelegate);

    private native void nativeRelease(long nativeRoomDelegate);
}
