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
        @Override
        public void onAudioTrackAdded(final RemoteParticipant remoteParticipant,
                                      final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackAdded");
            if (remoteAudioTrack == null) {
                logger.w("Received audio track added callback for non-existing audio track");
                return;
            }
            audioTracks.add(remoteAudioTrack);
            remoteAudioTracks.add(remoteAudioTrack);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackAdded(remoteParticipant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackRemoved(final RemoteParticipant remoteParticipant,
                                        final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackRemoved");
            remoteAudioTracks.remove(remoteAudioTrack);
            audioTracks.remove(remoteAudioTrack);
            if (remoteAudioTrack == null) {
                logger.w("Received audio track removed callback for non-existent audio track");
                return;
            }
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackRemoved(remoteParticipant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackAdded(final RemoteParticipant remoteParticipant,
                                      final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackAdded");
            if (remoteVideoTrack == null) {
                logger.w("Received video track added callback for non-existing video track");
                return;
            }
            videoTracks.add(remoteVideoTrack);
            remoteVideoTracks.add(remoteVideoTrack);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackAdded(remoteParticipant, remoteVideoTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackRemoved(final RemoteParticipant remoteParticipant,
                                        final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackRemoved");
            remoteVideoTracks.remove(remoteVideoTrack);
            videoTracks.remove(remoteVideoTrack);
            if (remoteVideoTrack == null) {
                logger.w("Received video track removed callback for non-existent video track");
                return;
            }
            remoteVideoTrack.release();
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackRemoved(remoteParticipant, remoteVideoTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackEnabled(final RemoteParticipant remoteParticipant,
                                        final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackEnabled");
            if (remoteAudioTrack == null) {
                logger.w("Received audio track enabled callback for non-existent audio track");
                return;
            }
            remoteAudioTrack.setEnabled(true);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackEnabled(remoteParticipant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onAudioTrackDisabled(final RemoteParticipant remoteParticipant,
                                         final RemoteAudioTrack remoteAudioTrack) {
            logger.d("onAudioTrackDisabled");
            if (remoteAudioTrack == null) {
                logger.w("Received audio track disabled callback for non-existent audio track");
                return;
            }
            remoteAudioTrack.setEnabled(false);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAudioTrackDisabled(remoteParticipant, remoteAudioTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackEnabled(final RemoteParticipant remoteParticipant,
                                        final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackEnabled");
            if (remoteVideoTrack == null) {
                logger.w("Received video track enabled callback for non-existent video track");
                return;
            }
            remoteVideoTrack.setEnabled(true);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackEnabled(remoteParticipant, remoteVideoTrack);
                    }
                });
            }
        }

        @Override
        public void onVideoTrackDisabled(final RemoteParticipant remoteParticipant,
                                         final RemoteVideoTrack remoteVideoTrack) {
            logger.d("onVideoTrackDisabled");
            if (remoteVideoTrack == null) {
                logger.w("Received video track disabled callback for non-existent video track");
                return;
            }
            remoteVideoTrack.setEnabled(false);
            final Listener listener = listenerReference.get();
            if (listener != null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onVideoTrackDisabled(remoteParticipant, remoteVideoTrack);
                    }
                });
            }
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
    public List<RemoteAudioTrack> getSubscribedAudioTracks() {
        return Collections.unmodifiableList(remoteAudioTracks);
    }

    /**
     * Returns read-only list of remote video tracks.
     *
     * @return list of video tracks.
     */
    public List<RemoteVideoTrack> getSubscribedVideoTracks() {
        return Collections.unmodifiableList(remoteVideoTracks);
    }

    /**
     * Set listener for this participant events.
     *
     * @param listener of participant events.
     */
    public void setListener(RemoteParticipant.Listener listener) {
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

    public interface Listener {
        /**
         * This method notifies the listener that a {@link RemoteParticipant} has added
         * an {@link RemoteAudioTrack} to this {@link Room}.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrack The audio track added to this room.
         */
        void onAudioTrackAdded(RemoteParticipant remoteParticipant,
                               RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed
         * an {@link RemoteAudioTrack} from this {@link Room}.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrack The audio track removed from this room.
         */
        void onAudioTrackRemoved(RemoteParticipant remoteParticipant,
                                 RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has added
         * an {@link RemoteVideoTrack} to this {@link Room}.
         *
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrack The video track added to this room.
         */
        void onVideoTrackAdded(RemoteParticipant remoteParticipant,
                               RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed
         * an {@link RemoteVideoTrack} from this {@link Room}.
         *
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrack The video track removed from this room.
         */
        void onVideoTrackRemoved(RemoteParticipant remoteParticipant,
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
