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
import android.support.annotation.NonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A participant represents a remote user that can connect to a {@link Room}.
 */
public class Participant {
    private static final Logger logger = Logger.getLogger(Participant.class);

    private final String identity;
    private final String sid;
    private final List<AudioTrack> audioTracks;
    private final List<VideoTrack> videoTracks;
    private final Handler handler;

    /*
     * We pass all native participant callbacks through the listener proxy and atomically
     * forward events to the developer listener.
     */
    private final AtomicReference<Listener> listenerReference = new AtomicReference<>(null);

    /*
     * The contract for Participant JNI callbacks is as follows:
     *
     * 1. All event callbacks are done on the same thread the developer used to connect to a room.
     * 2. Create and release all native memory on the same thread. In the case of a Participant,
     * VideoTracks are created and released on notifier thread.
     * 3. All Participant fields must be mutated on the developer's thread.
     *
     * Not abiding by this contract, may result in difficult to debug JNI crashes, incorrect return
     * values in the synchronous API methods, or missed callbacks. There is one test
     * `shouldReceiveTrackEventsIfListenerSetAfterEventReceived` that validates the scenario where
     * an audio track event would be missed if the callback is not posted to the developer's thread.
     */
    private final Listener participantListenerProxy = new Listener() {
        @Override
        public void onAudioTrackAdded(final Participant participant,
                                      final AudioTrack audioTrack) {
            checkCallback(participant, audioTrack, "onAudioTrackAdded");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onAudioTrackAdded");
                    audioTracks.add(audioTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackAdded(participant, audioTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackRemoved(final Participant participant,
                                        final AudioTrack audioTrack) {
            checkCallback(participant, audioTrack, "onAudioTrackRemoved");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onAudioTrackRemoved");
                    audioTracks.remove(audioTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackRemoved(participant, audioTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackAdded(final Participant participant,
                                      final VideoTrack videoTrack) {
            checkCallback(participant, videoTrack, "onVideoTrackAdded");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onVideoTrackAdded");
                    videoTracks.add(videoTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackAdded(participant, videoTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackRemoved(final Participant participant,
                                        final VideoTrack videoTrack) {
            checkCallback(participant, videoTrack, "onVideoTrackRemoved");

            // Release video track native memory on notifier
            videoTrack.release();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onVideoTrackRemoved");
                    videoTracks.remove(videoTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackRemoved(participant, videoTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackEnabled(final Participant participant,
                                        final AudioTrack audioTrack) {
            checkCallback(participant, audioTrack, "onAudioTrackEnabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onAudioTrackEnabled");
                    audioTrack.setEnabled(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackEnabled(participant, audioTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackDisabled(final Participant participant,
                                         final AudioTrack audioTrack) {
            checkCallback(participant, audioTrack, "onAudioTrackDisabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onAudioTrackDisabled");
                    audioTrack.setEnabled(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackDisabled(participant, audioTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackEnabled(final Participant participant,
                                        final VideoTrack videoTrack) {
            checkCallback(participant, videoTrack, "onVideoTrackEnabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onVideoTrackEnabled");
                    videoTrack.setEnabled(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackEnabled(participant, videoTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackDisabled(final Participant participant,
                                         final VideoTrack videoTrack) {
            checkCallback(participant, videoTrack, "onVideoTrackDisabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ThreadChecker.checkIsValidThread(handler);
                    logger.d("onVideoTrackDisabled");
                    videoTrack.setEnabled(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackDisabled(participant, videoTrack);
                    }
                }
            });
        }

        private void checkCallback(Participant participant, Track track, String callback) {
            Preconditions.checkState(participant != null, "Received null participant in %s",
                    callback);
            Preconditions.checkState(track != null, "Received null track in %s", callback);
        }
    };
    private long nativeParticipantContext;

    Participant(String identity,
                String sid,
                List<AudioTrack> audioTracks,
                List<VideoTrack> videoTracks,
                Handler handler,
                long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.audioTracks = audioTracks;
        this.videoTracks = videoTracks;
        this.handler = handler;
        this.nativeParticipantContext = nativeParticipantContext;
        nativeCreateParticipantListenerProxy(participantListenerProxy, nativeParticipantContext);
    }

    /**
     * Returns the identity of the participant.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the SID of a participant.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Retrieves the list of audio tracks.
     *
     * @return list of audio tracks.
     */
    public List<AudioTrack> getAudioTracks() {
        return audioTracks;
    }

    /**
     * Retrieves the list of video tracks.
     *
     * @return list of video tracks.
     */
    public List<VideoTrack> getVideoTracks() {
        return videoTracks;
    }

    /**
     * Set listener for this participant events.
     *
     * @param listener of participant events.
     */
    public void setListener(final @NonNull Participant.Listener listener) {
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
            for (VideoTrack videoTrack : videoTracks) {
                videoTrack.release();
            }
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    boolean isReleased() {
        return nativeParticipantContext == 0;
    }

    /**
     * Interface that provides {@link Participant} events.
     */
    public interface Listener {
        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link AudioTrack} to this {@link Room}.
         *
         * @param participant The participant object associated with this audio track.
         * @param audioTrack The audio track added to this room.
         */
        void onAudioTrackAdded(Participant participant,
                               AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * an {@link AudioTrack} from this {@link Room}.
         *
         * @param participant The participant object associated with this audio track.
         * @param audioTrack The audio track removed from this room.
         */
        void onAudioTrackRemoved(Participant participant,
                                 AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} has added
         * an {@link VideoTrack} to this {@link Room}.
         *
         * @param participant The participant object associated with this video track.
         * @param videoTrack The video track added to this room.
         */
        void onVideoTrackAdded(Participant participant,
                               VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} has removed
         * an {@link VideoTrack} from this {@link Room}. All {@link VideoRenderer}s of the
         * video track have been removed before receiving this callback to prevent native
         * memory leaks.
         *
         * @param participant The participant object associated with this video track.
         * @param videoTrack The video track removed from this room.
         */
        void onVideoTrackRemoved(Participant participant,
                                 VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} audio track
         * has been enabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param audioTrack The audio track enabled in this room.
         */
        void onAudioTrackEnabled(Participant participant, AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} audio track
         * has been disabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param audioTrack The audio track disabled in this room.
         */
        void onAudioTrackDisabled(Participant participant, AudioTrack audioTrack);

        /**
         * This method notifies the listener that a {@link Participant} video track
         * has been enabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param videoTrack The video track enabled in this room.
         */
        void onVideoTrackEnabled(Participant participant, VideoTrack videoTrack);

        /**
         * This method notifies the listener that a {@link Participant} video track
         * has been disabled.
         *
         * @param participant The participant object associated with this audio track.
         * @param videoTrack The video track disabled in this room.
         */
        void onVideoTrackDisabled(Participant participant, VideoTrack videoTrack);
    }

    private native void nativeCreateParticipantListenerProxy(Participant.Listener participantListenerProxy,
                                                             long nativeParticipantContext);
    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);
}
