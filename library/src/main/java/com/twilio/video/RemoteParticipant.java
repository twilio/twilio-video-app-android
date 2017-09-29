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
    private final List<RemoteAudioTrackPublication> remoteAudioTrackPublications;
    private final List<AudioTrackPublication> audioTrackPublications;
    private final List<RemoteVideoTrackPublication> remoteVideoTrackPublications;
    private final List<VideoTrackPublication> videoTrackPublications;
    private final List<RemoteDataTrackPublication> remoteDataTrackPublications;
    private final List<DataTrackPublication> dataTrackPublications;
    private final Handler handler;

    /*
     * All native participant callbacks are passed through the listener proxy and atomically
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
        public void onAudioTrackPublished(final RemoteParticipant remoteParticipant,
                                          final RemoteAudioTrackPublication remoteAudioTrackPublication) {
            checkCallback(remoteParticipant, remoteAudioTrackPublication, "onAudioTrackPublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackPublished");
                    audioTrackPublications.add(remoteAudioTrackPublication);
                    remoteAudioTrackPublications.add(remoteAudioTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackPublished(remoteParticipant,
                                remoteAudioTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackUnpublished(final RemoteParticipant remoteParticipant,
                                            final RemoteAudioTrackPublication remoteAudioTrackPublication) {
            checkCallback(remoteParticipant, remoteAudioTrackPublication, "onAudioTrackUnpublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackUnpublished");
                    audioTrackPublications.remove(remoteAudioTrackPublication);
                    remoteAudioTrackPublications.remove(remoteAudioTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackUnpublished(remoteParticipant,
                                remoteAudioTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackSubscribed(final RemoteParticipant remoteParticipant,
                                           final RemoteAudioTrackPublication remoteAudioTrackPublication,
                                           final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrackPublication, "onAudioTrackSubscribed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackSubscribed");
                    remoteAudioTrackPublication.setSubscribed(true);
                    remoteAudioTrackPublication.setRemoteAudioTrack(remoteAudioTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackSubscribed(remoteParticipant,
                                remoteAudioTrackPublication,
                                remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackUnsubscribed(final RemoteParticipant remoteParticipant,
                                             final RemoteAudioTrackPublication remoteAudioTrackPublication,
                                             final RemoteAudioTrack remoteAudioTrack) {
            checkCallback(remoteParticipant, remoteAudioTrackPublication, "onAudioTrackUnsubscribed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackUnsubscribed");
                    remoteAudioTrackPublication.setRemoteAudioTrack(null);
                    remoteAudioTrackPublication.setSubscribed(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackUnsubscribed(remoteParticipant,
                                remoteAudioTrackPublication,
                                remoteAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackPublished(final RemoteParticipant remoteParticipant,
                                          final RemoteVideoTrackPublication remoteVideoTrackPublication) {
            checkCallback(remoteParticipant, remoteVideoTrackPublication, "onVideoTrackPublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackPublished");
                    videoTrackPublications.add(remoteVideoTrackPublication);
                    remoteVideoTrackPublications.add(remoteVideoTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackPublished(remoteParticipant,
                                remoteVideoTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackUnpublished(final RemoteParticipant remoteParticipant,
                                            final RemoteVideoTrackPublication remoteVideoTrackPublication) {
            checkCallback(remoteParticipant, remoteVideoTrackPublication, "onVideoTrackUnpublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackUnpublished");
                    videoTrackPublications.remove(remoteVideoTrackPublication);
                    remoteVideoTrackPublications.remove(remoteVideoTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackUnpublished(remoteParticipant,
                                remoteVideoTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackSubscribed(final RemoteParticipant remoteParticipant,
                                           final RemoteVideoTrackPublication remoteVideoTrackPublication,
                                           final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrackPublication, "onVideoTrackSubscribed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackSubscribed");
                    remoteVideoTrackPublication.setSubscribed(true);
                    remoteVideoTrackPublication.setRemoteVideoTrack(remoteVideoTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackSubscribed(remoteParticipant,
                                remoteVideoTrackPublication,
                                remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackUnsubscribed(final RemoteParticipant remoteParticipant,
                                             final RemoteVideoTrackPublication remoteVideoTrackPublication,
                                             final RemoteVideoTrack remoteVideoTrack) {
            checkCallback(remoteParticipant, remoteVideoTrackPublication, "onVideoTrackUnsubscribed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackUnsubscribed");
                    remoteVideoTrack.release();
                    remoteVideoTrackPublication.setRemoteVideoTrack(null);
                    remoteVideoTrackPublication.setSubscribed(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackUnsubscribed(remoteParticipant,
                                remoteVideoTrackPublication,
                                remoteVideoTrack);
                    }
                }
            });
        }

        @Override
        public void onDataTrackPublished(final RemoteParticipant remoteParticipant,
                                         final RemoteDataTrackPublication remoteDataTrackPublication) {
            checkCallback(remoteParticipant, remoteDataTrackPublication, "onDataTrackPublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onDataTrackPublished");
                    dataTrackPublications.add(remoteDataTrackPublication);
                    remoteDataTrackPublications.add(remoteDataTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onDataTrackPublished(remoteParticipant,
                                remoteDataTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onDataTrackUnpublished(final RemoteParticipant remoteParticipant,
                                           final RemoteDataTrackPublication remoteDataTrackPublication) {
            checkCallback(remoteParticipant, remoteDataTrackPublication, "onDataTrackUnpublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onDataTrackUnpublished");
                    dataTrackPublications.remove(remoteDataTrackPublication);
                    remoteDataTrackPublications.remove(remoteDataTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onDataTrackUnpublished(remoteParticipant,
                                remoteDataTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onDataTrackSubscribed(final RemoteParticipant remoteParticipant,
                                          final RemoteDataTrackPublication remoteDataTrackPublication,
                                          final RemoteDataTrack remoteDataTrack) {
            checkCallback(remoteParticipant, remoteDataTrackPublication, "onDataTrackSubscribed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onDataTrackSubscribed");
                    remoteDataTrackPublication.setSubscribed(true);
                    remoteDataTrackPublication.setRemoteDataTrack(remoteDataTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onDataTrackSubscribed(remoteParticipant,
                                remoteDataTrackPublication,
                                remoteDataTrack);
                    }
                }
            });
        }

        @Override
        public void onDataTrackUnsubscribed(final RemoteParticipant remoteParticipant,
                                            final RemoteDataTrackPublication remoteDataTrackPublication,
                                            final RemoteDataTrack remoteDataTrack) {
            checkCallback(remoteParticipant, remoteDataTrackPublication, "onDataTrackUnsubscribed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onDataTrackUnsubscribed");
                    remoteDataTrack.release();
                    remoteDataTrackPublication.setRemoteDataTrack(null);
                    remoteDataTrackPublication.setSubscribed(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onDataTrackUnsubscribed(remoteParticipant,
                                remoteDataTrackPublication,
                                remoteDataTrack);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackEnabled(final RemoteParticipant remoteParticipant,
                                        final RemoteAudioTrackPublication remoteAudioTrackPublication) {
            checkCallback(remoteParticipant, remoteAudioTrackPublication, "onAudioTrackEnabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackEnabled");
                    remoteAudioTrackPublication.setEnabled(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackEnabled(remoteParticipant,
                                remoteAudioTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onAudioTrackDisabled(final RemoteParticipant remoteParticipant,
                                         final RemoteAudioTrackPublication remoteAudioTrackPublication) {
            checkCallback(remoteParticipant, remoteAudioTrackPublication, "onAudioTrackDisabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackDisabled");
                    remoteAudioTrackPublication.setEnabled(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackDisabled(remoteParticipant,
                                remoteAudioTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackEnabled(final RemoteParticipant remoteParticipant,
                                        final RemoteVideoTrackPublication remoteVideoTrackPublication) {
            checkCallback(remoteParticipant, remoteVideoTrackPublication, "onVideoTrackEnabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackEnabled");
                    remoteVideoTrackPublication.setEnabled(true);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackEnabled(remoteParticipant,
                                remoteVideoTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackDisabled(final RemoteParticipant remoteParticipant,
                                         final RemoteVideoTrackPublication remoteVideoTrackPublication) {
            checkCallback(remoteParticipant, remoteVideoTrackPublication, "onVideoTrackDisabled");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onVideoTrackDisabled");
                    remoteVideoTrackPublication.setEnabled(false);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackDisabled(remoteParticipant,
                                remoteVideoTrackPublication);
                    }
                }
            });
        }

        private void checkCallback(RemoteParticipant remoteParticipant,
                                   TrackPublication trackPublication,
                                   String callback) {
            Preconditions.checkState(remoteParticipant != null, "Received null remote " +
                            "participant in %s", callback);
            Preconditions.checkState(trackPublication != null, "Received null track publication " +
                    "in %s", callback);
        }
    };
    private long nativeParticipantContext;

    RemoteParticipant(String identity,
                      String sid,
                      List<RemoteAudioTrackPublication> remoteAudioTrackPublications,
                      List<RemoteVideoTrackPublication> remoteVideoTrackPublications,
                      List<RemoteDataTrackPublication> remoteDataTrackPublications,
                      Handler handler,
                      long nativeParticipantContext) {
        this.identity = identity;
        this.sid = sid;
        this.remoteAudioTrackPublications = remoteAudioTrackPublications;
        this.audioTrackPublications = new ArrayList<>(remoteAudioTrackPublications.size());
        addAudioTracks(remoteAudioTrackPublications);
        this.remoteVideoTrackPublications = remoteVideoTrackPublications;
        this.videoTrackPublications = new ArrayList<>(remoteVideoTrackPublications.size());
        addVideoTracks(remoteVideoTrackPublications);
        this.remoteDataTrackPublications = remoteDataTrackPublications;
        this.dataTrackPublications = new ArrayList<>(remoteDataTrackPublications.size());
        addDataTracks(remoteDataTrackPublications);
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
     * Returns read-only list of audio track publications.
     */
    @Override
    public List<AudioTrackPublication> getAudioTracks() {
        return Collections.unmodifiableList(audioTrackPublications);
    }

    /**
     * Returns read-only list of video track publications.
     */
    @Override
    public List<VideoTrackPublication> getVideoTracks() {
        return Collections.unmodifiableList(videoTrackPublications);
    }

    /**
     * Returns read-only list of data track publications.
     */
    @Override
    public List<DataTrackPublication> getDataTracks() {
        return Collections.unmodifiableList(dataTrackPublications);
    }

    /**
     * Returns read-only list of remote audio track publications.
     */
    public List<RemoteAudioTrackPublication> getRemoteAudioTracks() {
        return Collections.unmodifiableList(remoteAudioTrackPublications);
    }

    /**
     * Returns read-only list of remote video track publications.
     */
    public List<RemoteVideoTrackPublication> getRemoteVideoTracks() {
        return Collections.unmodifiableList(remoteVideoTrackPublications);
    }

    /**
     * Returns a read-only list of remote data track publications.
     */
    public List<RemoteDataTrackPublication> getRemoteDataTracks() {
        return Collections.unmodifiableList(remoteDataTrackPublications);
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
            for (RemoteVideoTrackPublication remoteVideoTrackPublication : remoteVideoTrackPublications) {
                RemoteVideoTrack remoteVideoTrack =
                        remoteVideoTrackPublication.getRemoteVideoTrack();

                if (remoteVideoTrack != null) {
                    remoteVideoTrack.release();
                }
            }
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    boolean isReleased() {
        return nativeParticipantContext == 0;
    }

    private void addAudioTracks(List<RemoteAudioTrackPublication> remoteAudioTracks) {
        for (RemoteAudioTrackPublication remoteAudioTrackPublication : remoteAudioTracks) {
            audioTrackPublications.add(remoteAudioTrackPublication);
        }
    }

    private void addVideoTracks(List<RemoteVideoTrackPublication> remoteVideoTracks) {
        for (RemoteVideoTrackPublication remoteVideoTrackPublication : remoteVideoTracks) {
            videoTrackPublications.add(remoteVideoTrackPublication);
        }
    }

    private void addDataTracks(List<RemoteDataTrackPublication> remoteDataTracks) {
        for (RemoteDataTrackPublication remoteDataTrackPublication : remoteDataTracks) {
            dataTrackPublications.add(remoteDataTrackPublication);
        }
    }

    /**
     * Interface that provides {@link RemoteParticipant} events.
     */
    public interface Listener {
        /**
         * This method notifies the listener that a {@link RemoteParticipant} has published
         * a {@link RemoteAudioTrack} to this {@link Room}. The audio of the track is not audible
         * until the track has been subscribed to.
         * @param remoteParticipant The participant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackPublished(RemoteParticipant remoteParticipant,
                                   RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has unpublished
         * a {@link RemoteAudioTrack} from this {@link Room}.
         * @param remoteParticipant The participant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackUnpublished(RemoteParticipant remoteParticipant,
                                     RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener the {@link RemoteAudioTrack} of the
         * {@link RemoteParticipant} has been subscribed to. The audio track is audible after
         * this callback.
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         * @param remoteAudioTrack The audio track subscribed to.
         */
        void onAudioTrackSubscribed(RemoteParticipant remoteParticipant,
                                    RemoteAudioTrackPublication remoteAudioTrackPublication,
                                    RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that the {@link RemoteAudioTrack} of the
         * {@link RemoteParticipant} has been unsubscribed from. The track is no longer audible
         * after being unsubscribed from the audio track.
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         * @param remoteAudioTrack The audio track unsubscribed from.
         */
        void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                      RemoteAudioTrackPublication remoteAudioTrackPublication,
                                      RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has published
         * a {@link RemoteVideoTrack} to this {@link Room}. Video frames will not begin flowing
         * until the video track has been subscribed to.
         * @param remoteParticipant The participant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackPublished(RemoteParticipant remoteParticipant,
                                   RemoteVideoTrackPublication remoteVideoTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed
         * a {@link RemoteVideoTrack} from this {@link Room}.
         * @param remoteParticipant The participant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackUnpublished(RemoteParticipant remoteParticipant,
                                     RemoteVideoTrackPublication remoteVideoTrackPublication);

        /**
         * This method notifies the listener the {@link RemoteVideoTrack} of the
         * {@link RemoteParticipant} has been subscribed to. Video frames are now flowing
         * and can be rendered.
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         * @param remoteVideoTrack The video track subscribed to.
         */
        void onVideoTrackSubscribed(RemoteParticipant remoteParticipant,
                                    RemoteVideoTrackPublication remoteVideoTrackPublication,
                                    RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that the {@link RemoteVideoTrack} of the
         * {@link RemoteParticipant} has been unsubscribed from. Video frames are no longer flowing.
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         * @param remoteVideoTrack The video track removed from this room.
         */
        void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                      RemoteVideoTrackPublication remoteVideoTrackPublication,
                                      RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has published
         * a {@link RemoteDataTrack} to this {@link Room}.
         * @param remoteParticipant The participant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         */
        void onDataTrackPublished(RemoteParticipant remoteParticipant,
                                  RemoteDataTrackPublication remoteDataTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed
         * a {@link RemoteDataTrack} from this {@link Room}.
         * @param remoteParticipant The participant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         */
        void onDataTrackUnpublished(RemoteParticipant remoteParticipant,
                                    RemoteDataTrackPublication remoteDataTrackPublication);

        /**
         * This method notifies the listener the {@link RemoteDataTrack} of the
         * {@link RemoteParticipant} has been subscribed to. Data track messages can be now be
         * received.
         * @param remoteParticipant The remoteParticipant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         * @param remoteDataTrack The data track subscribed to.
         */
        void onDataTrackSubscribed(RemoteParticipant remoteParticipant,
                                   RemoteDataTrackPublication remoteDataTrackPublication,
                                   RemoteDataTrack remoteDataTrack);

        /**
         * This method notifies the listener that the {@link RemoteDataTrack} of the
         * {@link RemoteParticipant} has been unsubscribed from. Data track messages will no longer
         * be received.
         * @param remoteParticipant The remoteParticipant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         * @param remoteDataTrack The data track removed from this room.
         */
        void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                     RemoteDataTrackPublication remoteDataTrackPublication,
                                     RemoteDataTrack remoteDataTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} audio track
         * has been enabled.
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                 RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} audio track
         * has been disabled.
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                  RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} video track
         * has been enabled.
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                 RemoteVideoTrackPublication remoteVideoTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} video track
         * has been disabled.
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                  RemoteVideoTrackPublication remoteVideoTrackPublication);
    }

    private native boolean nativeIsConnected(long nativeHandle);
    private native void nativeRelease(long nativeHandle);
}
