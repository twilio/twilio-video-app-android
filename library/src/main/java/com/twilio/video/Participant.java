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
    private final Listener participantListenerProxy = new Listener() {
        @Override
        public void onAudioTrackAdded(final Participant participant,
                                      final AudioTrack audioTrack) {
            logger.d("onAudioTrackAdded");
            if (audioTrack == null) {
                logger.w("Received audio track added callback for non-existing audio track");
                return;
            }
            audioTracks.add(audioTrack);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackAdded(participant, audioTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackRemoved(final Participant participant,
                                        final AudioTrack audioTrack) {
            logger.d("onAudioTrackRemoved");
            audioTracks.remove(audioTrack);
            if (audioTrack == null) {
                logger.w("Received audio track removed callback for non-existent audio track");
                return;
            }
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackRemoved(participant, audioTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackAdded(final Participant participant,
                                      final VideoTrack videoTrack) {
            logger.d("onVideoTrackAdded");
            if (videoTrack == null) {
                logger.w("Received video track added callback for non-existing video track");
                return;
            }
            videoTracks.add(videoTrack);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackAdded(participant, videoTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackRemoved(final Participant participant,
                                        final VideoTrack videoTrack) {
            logger.d("onVideoTrackRemoved");
            videoTracks.remove(videoTrack);
            if (videoTrack == null) {
                logger.w("Received video track removed callback for non-existent video track");
                return;
            }
            videoTrack.release();
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackRemoved(participant, videoTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackEnabled(final Participant participant,
                                        final AudioTrack audioTrack) {
            logger.d("onAudioTrackEnabled");
            if (audioTrack == null) {
                logger.w("Received audio track enabled callback for non-existent audio track");
                return;
            }
            audioTrack.setEnabled(true);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackEnabled(participant, audioTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackDisabled(final Participant participant,
                                         final AudioTrack audioTrack) {
            logger.d("onAudioTrackDisabled");
            if (audioTrack == null) {
                logger.w("Received audio track disabled callback for non-existent audio track");
                return;
            }
            audioTrack.setEnabled(false);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackDisabled(participant, audioTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackEnabled(final Participant participant,
                                        final VideoTrack videoTrack) {
            logger.d("onVideoTrackEnabled");
            if (videoTrack == null) {
                logger.w("Received video track enabled callback for non-existent video track");
                return;
            }
            videoTrack.setEnabled(true);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackEnabled(participant, videoTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackDisabled(final Participant participant,
                                         final VideoTrack videoTrack) {
            logger.d("onVideoTrackDisabled");
            if (videoTrack == null) {
                logger.w("Received video track disabled callback for non-existent video track");
                return;
            }
            videoTrack.setEnabled(false);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackDisabled(participant, videoTrack);
                    }
                });
            }
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
    public void setListener(Participant.Listener listener) {
        listenerReference.set(listener);
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
         * an {@link VideoTrack} from this {@link Room}.
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
