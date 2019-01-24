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
import android.support.annotation.Nullable;
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
    private Room.State roomState;
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
                public void onConnected(@NonNull final Room room) {
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onConnected()");
                                Room.this.listener.onConnected(room);
                            });
                }

                @Override
                public void onConnectFailure(
                        @NonNull final Room room, @NonNull final TwilioException twilioException) {
                    // Release native room
                    releaseRoom();

                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onConnectFailure()");

                                // Update room state
                                Room.this.roomState = Room.State.DISCONNECTED;

                                // Release native room delegate
                                release();

                                // Notify developer
                                Room.this.listener.onConnectFailure(room, twilioException);
                            });
                }

                @Override
                public void onReconnecting(
                        @NonNull Room room, @NonNull TwilioException twilioException) {
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onReconnecting()");

                                // Update room state
                                Room.this.roomState = State.RECONNECTING;

                                // Notify developer
                                Room.this.listener.onReconnecting(room, twilioException);
                            });
                }

                @Override
                public void onReconnected(@NonNull Room room) {
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onReconnected()");

                                // Notify developer
                                Room.this.roomState = State.CONNECTED;
                                Room.this.listener.onReconnected(room);
                            });
                }

                @Override
                public void onDisconnected(
                        @NonNull final Room room, @Nullable final TwilioException twilioException) {
                    // Release native room
                    releaseRoom();

                    // Ensure the local participant is released if the disconnect was issued by the
                    // core
                    if (localParticipant != null) {
                        localParticipant.release();
                    }

                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onDisconnected()");

                                // Update room state
                                Room.this.roomState = Room.State.DISCONNECTED;

                                // Release native room delegate
                                release();

                                // Notify developer
                                Room.this.listener.onDisconnected(room, twilioException);
                            });
                }

                @Override
                public void onParticipantConnected(
                        @NonNull final Room room, @NonNull final RemoteParticipant participant) {
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onParticipantConnected()");

                                // Update participants
                                participantMap.put(participant.getSid(), participant);

                                // Notify developer
                                Room.this.listener.onParticipantConnected(room, participant);
                            });
                }

                @Override
                public void onParticipantDisconnected(
                        @NonNull final Room room,
                        @NonNull final RemoteParticipant remoteParticipant) {
                    // Release participant
                    remoteParticipant.release();

                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onParticipantDisconnected()");

                                // Update participants
                                participantMap.remove(remoteParticipant.getSid());

                                // Notify developer
                                Room.this.listener.onParticipantDisconnected(
                                        room, remoteParticipant);
                            });
                }

                @Override
                public void onRecordingStarted(@NonNull final Room room) {
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onRecordingStarted()");
                                Room.this.listener.onRecordingStarted(room);
                            });
                }

                @Override
                public void onRecordingStopped(@NonNull final Room room) {
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onRecordingStopped()");
                                Room.this.listener.onRecordingStopped(room);
                            });
                }
            };
    private final StatsListener statsListenerProxy =
            statsReports -> {
                final Pair<Handler, StatsListener> statsPair = Room.this.statsListenersQueue.poll();
                if (statsPair != null) {
                    statsPair.first.post(() -> statsPair.second.onStats(statsReports));
                }
            };

    Room(
            @NonNull Context context,
            @NonNull String name,
            @NonNull Handler handler,
            @NonNull Listener listener) {
        this.context = context;
        this.name = name;
        this.sid = "";
        this.roomState = Room.State.DISCONNECTED;
        this.listener = listener;
        this.handler = handler;
        this.statsListenersQueue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Returns the name of the current room. This method will return the SID if the room was created
     * without a name.
     */
    @NonNull
    public String getName() {
        return name;
    }

    /** Returns the SID of the current room. */
    @NonNull
    public String getSid() {
        return sid;
    }

    /** Returns the current room state. */
    @NonNull
    public synchronized Room.State getState() {
        return roomState;
    }

    /** Returns whether any media in the Room is being recorded. */
    public synchronized boolean isRecording() {
        return roomState == Room.State.CONNECTED && nativeIsRecording(nativeRoomDelegate);
    }

    /**
     * Returns all currently connected participants.
     *
     * @return list of participants.
     */
    @NonNull
    public synchronized List<RemoteParticipant> getRemoteParticipants() {
        return new ArrayList<>(participantMap.values());
    }

    /**
     * Returns the current local participant. If the room has not reached {@link
     * Room.State#CONNECTED} then this method will return null.
     */
    @Nullable
    public synchronized LocalParticipant getLocalParticipant() {
        return localParticipant;
    }

    /**
     * Retrieve stats for all media tracks and notify {@link StatsListener} via calling thread. In
     * case where room is in {@link Room.State#DISCONNECTED} state, reports won't be delivered.
     *
     * @param statsListener listener that receives stats reports for all media tracks.
     */
    public synchronized void getStats(@NonNull StatsListener statsListener) {
        Preconditions.checkNotNull(statsListener, "StatsListener must not be null");
        if (roomState == Room.State.DISCONNECTED) {
            return;
        }
        statsListenersQueue.offer(new Pair<>(Util.createCallbackHandler(), statsListener));
        nativeGetStats(nativeRoomDelegate);
    }

    /** Disconnects from the room. */
    public synchronized void disconnect() {
        if (roomState != Room.State.DISCONNECTED && nativeRoomDelegate != 0) {
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
    void connect(@NonNull final ConnectOptions connectOptions) {
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
            roomState = Room.State.CONNECTING;
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
        this.roomState = Room.State.CONNECTED;
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
                    () -> listenerPair.second.onStats(new ArrayList<StatsReport>()));
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
        void onConnected(@NonNull Room room);

        /**
         * Called when a connection to a room failed.
         *
         * @param room the room that failed to be connected to.
         * @param twilioException an exception describing why connect failed.
         */
        void onConnectFailure(@NonNull Room room, @NonNull TwilioException twilioException);

        /**
         * Called when the {@link LocalParticipant} has experienced a network disruption and the
         * client begins trying to reestablish a connection to a room.
         *
         * <p>The SDK groups network disruptions into two categories: signaling and media. The
         * occurrence of either of these network disruptions will result in the onReconnecting
         * callback. During a media reconnection certain signaling related method could continue to
         * be invoked as documented below:
         *
         * <p>The following {@link Room.Listener} events could be invoked:
         *
         * <ul>
         *   <li>{@link Room.Listener#onDisconnected(Room, TwilioException)}
         *   <li>{@link Room.Listener#onParticipantConnected(Room, RemoteParticipant)}
         *   <li>{@link Room.Listener#onParticipantDisconnected(Room, RemoteParticipant)}
         *   <li>{@link Room.Listener#onRecordingStarted(Room)}
         *   <li>{@link Room.Listener#onRecordingStopped(Room)}
         * </ul>
         *
         * <p>All {@link LocalParticipant.Listener} methods could be invoked
         *
         * <p>All {@link RemoteParticipant.Listener} methods except the following could be invoked:
         *
         * <p>
         *
         * <ul>
         *   <li>{@link RemoteParticipant.Listener#onDataTrackSubscribed(RemoteParticipant,
         *       RemoteDataTrackPublication, RemoteDataTrack)}
         *   <li>{@link RemoteParticipant.Listener#onAudioTrackSubscribed(RemoteParticipant,
         *       RemoteAudioTrackPublication, RemoteAudioTrack)}
         *   <li>{@link RemoteParticipant.Listener#onVideoTrackSubscribed(RemoteParticipant,
         *       RemoteVideoTrackPublication, RemoteVideoTrack)}
         * </ul>
         *
         * @param room the room the {@link LocalParticipant} is attempting to reconnect to.
         * @param twilioException An error explaining why the {@link LocalParticipant} is
         *     reconnecting to a room. Errors are limited to {@link
         *     TwilioException#SIGNALING_CONNECTION_DISCONNECTED_EXCEPTION} and {@link
         *     TwilioException#MEDIA_CONNECTION_ERROR_EXCEPTION}.
         */
        void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException);

        /**
         * Called after the {@link LocalParticipant} reconnects to a room after a network
         * disruption.
         *
         * @param room the room that was reconnected.
         */
        void onReconnected(@NonNull Room room);

        /**
         * Called when a room has been disconnected from.
         *
         * @param room the room that was disconnected from.
         * @param twilioException An exception if there was a problem that caused the room to be
         *     disconnected from. This value will be null is there were no problems disconnecting
         *     from the room.
         */
        void onDisconnected(@NonNull Room room, @Nullable TwilioException twilioException);

        /**
         * Called when a participant has connected to a room.
         *
         * @param room the room the participant connected to.
         * @param remoteParticipant the newly connected participant.
         */
        void onParticipantConnected(
                @NonNull Room room, @NonNull RemoteParticipant remoteParticipant);

        /**
         * Called when a participant has disconnected from a room. The disconnected participant's
         * audio and video tracks will still be available in their last known state. Video tracks
         * renderers are removed when a participant is disconnected.
         *
         * @param room the room the participant disconnected from.
         * @param remoteParticipant the disconnected participant.
         */
        void onParticipantDisconnected(
                @NonNull Room room, @NonNull RemoteParticipant remoteParticipant);

        /**
         * This method is only called when a {@link Room} which was not previously recording starts
         * recording. If you've joined a {@link Room} which is already recording this event will not
         * be fired.
         *
         * @param room
         */
        void onRecordingStarted(@NonNull Room room);

        /**
         * This method is only called when a {@link Room} which was previously recording stops
         * recording. If you've joined a {@link Room} which is not recording this event will not be
         * fired.
         *
         * @param room
         */
        void onRecordingStopped(@NonNull Room room);
    }

    /** Represents the current state of a {@link Room}. */
    public enum State {
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        DISCONNECTED
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
