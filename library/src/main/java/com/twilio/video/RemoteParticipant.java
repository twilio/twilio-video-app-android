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

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A participant represents a remote user that can connect to a {@link Room}.
 */
public class RemoteParticipant implements Participant {
    private static final Logger logger = Logger.getLogger(RemoteParticipant.class);

    private final String identity;
    private final String sid;
    private final List<RemoteAudioTrack> remoteAudioTracks;
    private final List<AudioTrack> audioTracks;
    private final List<RemoteVideoTrack> remoteVideoTracks;
    private final List<VideoTrack> videoTracks;
    private final Handler handler;

    /*
     * We pass all native participant callbacks through the listener proxy and atomically
     * forward events to the developer listener.
     */
    private final AtomicReference<Listener> listenerReference = new AtomicReference<>(null);

    /*
     * This listener proxy is bound at the JNI level.
     */
    @SuppressWarnings("unused")
    private final Listener participantListenerProxy = new Listener() {

        /*
         * All event processing is done on the same thread the developer used to connect to a
         * room. All operations that modify the state of the participant MUST BE PERFORMED ON THE
         * DEVELOPER'S THREAD. This is required because we have both an asynchronous and synchronous
         * API and it is possible that the developer could use the synchronous API before receiving
         * an asynchronous event. We currently only have one test
         * `shouldReceiveTrackEventsIfListenerSetAfterEventReceived` that validates this scenario
         * with the audio track added event.
         */

        @Override
        public void onAudioTrackAdded(final RemoteParticipant remoteParticipant,
                                      final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrack, "onAudioTrackAdded");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackAdded");
                    audioTracks.add(remoteAudioTrack);
                    remoteAudioTracks.add(remoteAudioTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackAdded(remoteParticipant, remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackRemoved(final RemoteParticipant remoteParticipant,
                                        final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrack, "onAudioTrackRemoved");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackRemoved");
                    audioTracks.remove(remoteAudioTrack);
                    remoteAudioTracks.remove(remoteAudioTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackRemoved(remoteParticipant, remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onSubscribedToAudioTrack(final RemoteParticipant remoteParticipant,
                                             final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrack, "onSubscribedToAudioTrack");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onSubscribedToAudioTrack");
                    remoteAudioTrack.setSubscribed(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onSubscribedToAudioTrack(remoteParticipant, remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onUnsubscribedFromAudioTrack(final RemoteParticipant remoteParticipant,
                                                 final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrack, "onUnsubscribedFromAudioTrack");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onUnsubscribedFromAudioTrack");
                    remoteAudioTrack.setSubscribed(false);
                    remoteAudioTrack.invalidateWebRtcTrack();
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onUnsubscribedFromAudioTrack(remoteParticipant, remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackAdded(final RemoteParticipant remoteParticipant,
                                      final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrack, "onVideoTrackAdded");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackAdded");
                    videoTracks.add(remoteVideoTrack);
                    remoteVideoTracks.add(remoteVideoTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackAdded(remoteParticipant, remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackRemoved(final RemoteParticipant remoteParticipant,
                                        final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrack, "onVideoTrackRemoved");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackRemoved");
                    videoTracks.remove(remoteVideoTrack);
                    remoteVideoTracks.remove(remoteVideoTrack);
                    remoteVideoTrack.release();
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackRemoved(remoteParticipant, remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onSubscribedToVideoTrack(final RemoteParticipant remoteParticipant,
                                             final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrack, "onSubscribedToVideoTrack");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onSubscribedToVideoTrack");
                    remoteVideoTrack.setSubscribed(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onSubscribedToVideoTrack(remoteParticipant, remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onUnsubscribedFromVideoTrack(final RemoteParticipant remoteParticipant,
                                                 final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrack, "onUnsubscribedFromVideoTrack");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onUnsubscribedFromVideoTrack");
                    remoteVideoTrack.setSubscribed(false);
                    remoteVideoTrack.invalidateWebRtcTrack();
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onUnsubscribedFromVideoTrack(remoteParticipant, remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackEnabled(final RemoteParticipant remoteParticipant,
                                        final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrack, "onAudioTrackEnabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackEnabled");
                    remoteAudioTrack.setEnabled(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackEnabled(remoteParticipant, remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackDisabled(final RemoteParticipant remoteParticipant,
                                         final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrack, "onAudioTrackDisabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackDisabled");
                    remoteAudioTrack.setEnabled(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackDisabled(remoteParticipant, remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackEnabled(final RemoteParticipant remoteParticipant,
                                        final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrack, "onVideoTrackEnabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackEnabled");
                    remoteVideoTrack.setEnabled(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackEnabled(remoteParticipant, remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackDisabled(final RemoteParticipant remoteParticipant,
                                         final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrack, "onVideoTrackDisabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackDisabled");
                    remoteVideoTrack.setEnabled(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackDisabled(remoteParticipant, remoteVideoTrack);
                    }
                }
            });
        }

        private void checkCallback(RemoteParticipant remoteParticipant,
                                   Track track,
                                   String callback) {
            Preconditions.checkState(remoteParticipant != null, "Received null remote " +
                            "participant in %s", callback);
            Preconditions.checkState(track != null, "Received null track in %s", callback);
        }
    };
    private long nativeParticipantContext;

    RemoteParticipant(String identity,
                      String sid,
                      List<RemoteAudioTrack> remoteAudioTracks,
                      List<RemoteVideoTrack> remoteVideoTracks,
                      Handler handler,
                      long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.remoteAudioTracks = remoteAudioTracks;
        this.audioTracks = new ArrayList<>(remoteAudioTracks.size());
        addAudioTracks(remoteAudioTracks);
        this.remoteVideoTracks = remoteVideoTracks;
        this.videoTracks = new ArrayList<>(remoteVideoTracks.size());
        addVideoTracks(remoteVideoTracks);
        this.handler = handler;
        this.nativeParticipantContext = nativeParticipantContext;
    }

    /**
     * Returns the SID of a remote participant.
     */
    @Override
    public String getSid() {
        return sid;
    }

    /**
     * Returns the identity of the remote participant.
     */
    @Override
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns read-only list of audio tracks.
     */
    @Override
    public List<AudioTrack> getAudioTracks() {
        return Collections.unmodifiableList(audioTracks);
    }

    /**
     * Returns read-only list of video tracks.
     */
    @Override
    public List<VideoTrack> getVideoTracks() {
        return Collections.unmodifiableList(videoTracks);
    }

    /**
     * Returns read-only list of remote audio tracks.
     *
     * @return list of audio tracks.
     */
    public List<RemoteAudioTrack> getRemoteAudioTracks() {
        return Collections.unmodifiableList(remoteAudioTracks);
    }

    /**
     * Returns read-only list of remote video tracks.
     *
     * @return list of video tracks.
     */
    public List<RemoteVideoTrack> getRemoteVideoTracks() {
        return Collections.unmodifiableList(remoteVideoTracks);
    }

    /**
     * Set listener for this participant events.
     *
     * @param listener of participant events.
     */
    public void setListener(RemoteParticipant.Listener listener) {
        Preconditions.checkNotNull(listener, "Listener must not be null");

        this.listenerReference.set(listener);
    }

    /**
     * Checks if the participant is connected to a room.
     *
     * @return true if the participant is connected to a room and false if not.
     */
    public synchronized boolean isConnected() {
        if (!isReleased()) {
            return nativeIsConnected(nativeParticipantContext);
        } else {
            return false;
        }
    }

    synchronized void release() {
        if (!isReleased()) {
            for (RemoteVideoTrack remoteVideoTrack : remoteVideoTracks) {
                remoteVideoTrack.release();
            }
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    boolean isReleased() {
        return nativeParticipantContext == 0;
    }

    private void addAudioTracks(List<RemoteAudioTrack> remoteAudioTracks) {
        for (RemoteAudioTrack remoteAudioTrack : remoteAudioTracks) {
            audioTracks.add(remoteAudioTrack);
        }
    }

    private void addVideoTracks(List<RemoteVideoTrack> remoteVideoTracks) {
        for (RemoteVideoTrack remoteVideoTrack : remoteVideoTracks) {
            videoTracks.add(remoteVideoTrack);
        }
    }

    /**
     * Interface that provides {@link RemoteParticipant} events.
     */
    public interface Listener {
        /**
         * This method notifies the listener that a {@link RemoteParticipant} has added
         * a {@link RemoteAudioTrack} to this {@link Room}. The audio of the track is not audible
         * until the track has been subscribed to.
         *
         * @param remoteParticipant The participant object associated with this audio track.
         * @param remoteAudioTrack The audio track added to this room.
         */
        void onAudioTrackAdded(RemoteParticipant remoteParticipant,
                               RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed
         * a {@link RemoteAudioTrack} from this {@link Room}.
         *
         * @param remoteParticipant The participant object associated with this audio track.
         * @param remoteAudioTrack The audio track removed from this room.
         */
        void onAudioTrackRemoved(RemoteParticipant remoteParticipant,
                                 RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener the {@link RemoteAudioTrack} of the
         * {@link RemoteParticipant} has been subscribed to. The audio track is audible after
         * this callback.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrack The audio track subscribed to.
         */
        void onSubscribedToAudioTrack(RemoteParticipant remoteParticipant,
                                      RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that the {@link RemoteAudioTrack} of the
         * {@link RemoteParticipant} has been unsubscribed from. The track is no longer audible
         * after being unsubscribed from the audio track.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrack The audio track unsubscribed from.
         */
        void onUnsubscribedFromAudioTrack(RemoteParticipant remoteParticipant,
                                          RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has added
         * a {@link RemoteVideoTrack} to this {@link Room}. Video frames will not begin flowing
         * until the video track has been subscribed to.
         *
         * @param remoteParticipant The participant object associated with this video track.
         * @param remoteVideoTrack The video track added to this room.
         */
        void onVideoTrackAdded(RemoteParticipant remoteParticipant,
                               RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed
         * a {@link RemoteVideoTrack} from this {@link Room}.
         *
         * @param remoteParticipant The participant object associated with this video track.
         * @param remoteVideoTrack The video track removed from this room.
         */
        void onVideoTrackRemoved(RemoteParticipant remoteParticipant,
                                 RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener the {@link RemoteVideoTrack} of the
         * {@link RemoteParticipant} has been subscribed to. Video frames are now flowing
         * and can be rendered.
         *
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrack The video track subscribed to.
         */
        void onSubscribedToVideoTrack(RemoteParticipant remoteParticipant,
                                      RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that the {@link RemoteVideoTrack} of the
         * {@link RemoteParticipant} has been unsubscribed from. Video frames are no longer flowing.
         *
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrack The video track removed from this room.
         */
        void onUnsubscribedFromVideoTrack(RemoteParticipant remoteParticipant,
                                          RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} audio track
         * has been enabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrack The audio track enabled in this room.
         */
        void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                 RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} audio track
         * has been disabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrack The audio track disabled in this room.
         */
        void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                  RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} video track
         * has been enabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteVideoTrack The video track enabled in this room.
         */
        void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                 RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} video track
         * has been disabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteVideoTrack The video track disabled in this room.
         */
        void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                  RemoteVideoTrack remoteVideoTrack);
    }

    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);
}
