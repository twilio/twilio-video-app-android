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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** A participant represents a remote user that can connect to a {@link Room}. */
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
     *
     * The contract for RemoteParticipant JNI callbacks is as follows:
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
    @SuppressWarnings("unused")
    private final Listener participantListenerProxy =
            new Listener() {
                @Override
                public void onAudioTrackPublished(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication) {
                    checkCallback(
                            remoteParticipant,
                            remoteAudioTrackPublication,
                            "onAudioTrackPublished");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackPublished");
                                audioTrackPublications.add(remoteAudioTrackPublication);
                                remoteAudioTrackPublications.add(remoteAudioTrackPublication);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackPublished(
                                            remoteParticipant, remoteAudioTrackPublication);
                                }
                            });
                }

                @Override
                public void onAudioTrackUnpublished(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication) {
                    checkCallback(
                            remoteParticipant,
                            remoteAudioTrackPublication,
                            "onAudioTrackUnpublished");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackUnpublished");
                                audioTrackPublications.remove(remoteAudioTrackPublication);
                                remoteAudioTrackPublications.remove(remoteAudioTrackPublication);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackUnpublished(
                                            remoteParticipant, remoteAudioTrackPublication);
                                }
                            });
                }

                @Override
                public void onAudioTrackSubscribed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication,
                        @NonNull final RemoteAudioTrack remoteAudioTrack) {
                    checkCallback(
                            remoteParticipant,
                            remoteAudioTrackPublication,
                            "onAudioTrackSubscribed");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackSubscribed");
                                remoteAudioTrackPublication.setSubscribed(true);
                                remoteAudioTrackPublication.setRemoteAudioTrack(remoteAudioTrack);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackSubscribed(
                                            remoteParticipant,
                                            remoteAudioTrackPublication,
                                            remoteAudioTrack);
                                }
                            });
                }

                @Override
                public void onAudioTrackSubscriptionFailed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication,
                        @NonNull final TwilioException twilioException) {
                    checkCallback(
                            remoteParticipant,
                            remoteAudioTrackPublication,
                            "onAudioTrackSubscriptionFailed");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackSubscriptionFailed");
                                remoteAudioTrackPublication.setSubscribed(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackSubscriptionFailed(
                                            remoteParticipant,
                                            remoteAudioTrackPublication,
                                            twilioException);
                                }
                            });
                }

                @Override
                public void onAudioTrackUnsubscribed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication,
                        @NonNull final RemoteAudioTrack remoteAudioTrack) {
                    checkCallback(
                            remoteParticipant,
                            remoteAudioTrackPublication,
                            "onAudioTrackUnsubscribed");

                    // Release audio track on notifier thread
                    remoteAudioTrack.release();

                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackUnsubscribed");
                                remoteAudioTrackPublication.setRemoteAudioTrack(null);
                                remoteAudioTrackPublication.setSubscribed(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackUnsubscribed(
                                            remoteParticipant,
                                            remoteAudioTrackPublication,
                                            remoteAudioTrack);
                                }
                            });
                }

                @Override
                public void onVideoTrackPublished(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication) {
                    checkCallback(
                            remoteParticipant,
                            remoteVideoTrackPublication,
                            "onVideoTrackPublished");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackPublished");
                                videoTrackPublications.add(remoteVideoTrackPublication);
                                remoteVideoTrackPublications.add(remoteVideoTrackPublication);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackPublished(
                                            remoteParticipant, remoteVideoTrackPublication);
                                }
                            });
                }

                @Override
                public void onVideoTrackUnpublished(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication) {
                    checkCallback(
                            remoteParticipant,
                            remoteVideoTrackPublication,
                            "onVideoTrackUnpublished");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackUnpublished");
                                videoTrackPublications.remove(remoteVideoTrackPublication);
                                remoteVideoTrackPublications.remove(remoteVideoTrackPublication);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackUnpublished(
                                            remoteParticipant, remoteVideoTrackPublication);
                                }
                            });
                }

                @Override
                public void onVideoTrackSubscribed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication,
                        @NonNull final RemoteVideoTrack remoteVideoTrack) {
                    checkCallback(
                            remoteParticipant,
                            remoteVideoTrackPublication,
                            "onVideoTrackSubscribed");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackSubscribed");
                                remoteVideoTrackPublication.setSubscribed(true);
                                remoteVideoTrackPublication.setRemoteVideoTrack(remoteVideoTrack);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackSubscribed(
                                            remoteParticipant,
                                            remoteVideoTrackPublication,
                                            remoteVideoTrack);
                                }
                            });
                }

                @Override
                public void onVideoTrackSubscriptionFailed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication,
                        @NonNull final TwilioException twilioException) {
                    checkCallback(
                            remoteParticipant,
                            remoteVideoTrackPublication,
                            "onVideoTrackSubscriptionFailed");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackSubscriptionFailed");
                                remoteVideoTrackPublication.setSubscribed(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackSubscriptionFailed(
                                            remoteParticipant,
                                            remoteVideoTrackPublication,
                                            twilioException);
                                }
                            });
                }

                @Override
                public void onVideoTrackUnsubscribed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication,
                        @NonNull final RemoteVideoTrack remoteVideoTrack) {
                    checkCallback(
                            remoteParticipant,
                            remoteVideoTrackPublication,
                            "onVideoTrackUnsubscribed");

                    // Release video track native memory on notifier
                    remoteVideoTrack.release();

                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackUnsubscribed");
                                remoteVideoTrackPublication.setRemoteVideoTrack(null);
                                remoteVideoTrackPublication.setSubscribed(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackUnsubscribed(
                                            remoteParticipant,
                                            remoteVideoTrackPublication,
                                            remoteVideoTrack);
                                }
                            });
                }

                @Override
                public void onDataTrackPublished(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteDataTrackPublication remoteDataTrackPublication) {
                    checkCallback(
                            remoteParticipant, remoteDataTrackPublication, "onDataTrackPublished");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onDataTrackPublished");
                                dataTrackPublications.add(remoteDataTrackPublication);
                                remoteDataTrackPublications.add(remoteDataTrackPublication);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onDataTrackPublished(
                                            remoteParticipant, remoteDataTrackPublication);
                                }
                            });
                }

                @Override
                public void onDataTrackUnpublished(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteDataTrackPublication remoteDataTrackPublication) {
                    checkCallback(
                            remoteParticipant,
                            remoteDataTrackPublication,
                            "onDataTrackUnpublished");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onDataTrackUnpublished");
                                dataTrackPublications.remove(remoteDataTrackPublication);
                                remoteDataTrackPublications.remove(remoteDataTrackPublication);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onDataTrackUnpublished(
                                            remoteParticipant, remoteDataTrackPublication);
                                }
                            });
                }

                @Override
                public void onDataTrackSubscribed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteDataTrackPublication remoteDataTrackPublication,
                        @NonNull final RemoteDataTrack remoteDataTrack) {
                    checkCallback(
                            remoteParticipant, remoteDataTrackPublication, "onDataTrackSubscribed");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onDataTrackSubscribed");
                                remoteDataTrackPublication.setSubscribed(true);
                                remoteDataTrackPublication.setRemoteDataTrack(remoteDataTrack);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onDataTrackSubscribed(
                                            remoteParticipant,
                                            remoteDataTrackPublication,
                                            remoteDataTrack);
                                }
                            });
                }

                @Override
                public void onDataTrackSubscriptionFailed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteDataTrackPublication remoteDataTrackPublication,
                        @NonNull final TwilioException twilioException) {
                    checkCallback(
                            remoteParticipant,
                            remoteDataTrackPublication,
                            "onDataTrackSubscriptionFailed");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onDataTrackSubscriptionFailed");
                                remoteDataTrackPublication.setSubscribed(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onDataTrackSubscriptionFailed(
                                            remoteParticipant,
                                            remoteDataTrackPublication,
                                            twilioException);
                                }
                            });
                }

                @Override
                public void onDataTrackUnsubscribed(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteDataTrackPublication remoteDataTrackPublication,
                        @NonNull final RemoteDataTrack remoteDataTrack) {
                    checkCallback(
                            remoteParticipant,
                            remoteDataTrackPublication,
                            "onDataTrackUnsubscribed");

                    // Release remote data track on notifier
                    remoteDataTrack.release();

                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onDataTrackUnsubscribed");
                                remoteDataTrackPublication.setRemoteDataTrack(null);
                                remoteDataTrackPublication.setSubscribed(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onDataTrackUnsubscribed(
                                            remoteParticipant,
                                            remoteDataTrackPublication,
                                            remoteDataTrack);
                                }
                            });
                }

                @Override
                public void onAudioTrackEnabled(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication) {
                    checkCallback(
                            remoteParticipant, remoteAudioTrackPublication, "onAudioTrackEnabled");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackEnabled");
                                remoteAudioTrackPublication.setEnabled(true);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackEnabled(
                                            remoteParticipant, remoteAudioTrackPublication);
                                }
                            });
                }

                @Override
                public void onAudioTrackDisabled(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteAudioTrackPublication remoteAudioTrackPublication) {
                    checkCallback(
                            remoteParticipant, remoteAudioTrackPublication, "onAudioTrackDisabled");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onAudioTrackDisabled");
                                remoteAudioTrackPublication.setEnabled(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onAudioTrackDisabled(
                                            remoteParticipant, remoteAudioTrackPublication);
                                }
                            });
                }

                @Override
                public void onVideoTrackEnabled(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication) {
                    checkCallback(
                            remoteParticipant, remoteVideoTrackPublication, "onVideoTrackEnabled");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackEnabled");
                                remoteVideoTrackPublication.setEnabled(true);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackEnabled(
                                            remoteParticipant, remoteVideoTrackPublication);
                                }
                            });
                }

                @Override
                public void onVideoTrackDisabled(
                        @NonNull final RemoteParticipant remoteParticipant,
                        @NonNull final RemoteVideoTrackPublication remoteVideoTrackPublication) {
                    checkCallback(
                            remoteParticipant, remoteVideoTrackPublication, "onVideoTrackDisabled");
                    handler.post(
                            () -> {
                                ThreadChecker.checkIsValidThread(handler);
                                logger.d("onVideoTrackDisabled");
                                remoteVideoTrackPublication.setEnabled(false);
                                Listener listener = listenerReference.get();

                                if (listener != null) {
                                    listener.onVideoTrackDisabled(
                                            remoteParticipant, remoteVideoTrackPublication);
                                }
                            });
                }

                private void checkCallback(
                        RemoteParticipant remoteParticipant,
                        TrackPublication trackPublication,
                        String callback) {
                    Preconditions.checkState(
                            remoteParticipant != null,
                            "Received null remote " + "participant in %s",
                            callback);
                    Preconditions.checkState(
                            trackPublication != null,
                            "Received null track publication " + "in %s",
                            callback);
                }
            };

    private long nativeParticipantContext;

    /** Returns the SID of a remote participant. */
    @NonNull
    @Override
    public String getSid() {
        return sid;
    }

    RemoteParticipant(
            String identity,
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

    /** Returns the identity of the remote participant. */
    @NonNull
    @Override
    public String getIdentity() {
        return identity;
    }

    /** Returns read-only list of audio track publications. */
    @NonNull
    @Override
    public List<AudioTrackPublication> getAudioTracks() {
        return Collections.unmodifiableList(audioTrackPublications);
    }

    /** Returns read-only list of video track publications. */
    @NonNull
    @Override
    public List<VideoTrackPublication> getVideoTracks() {
        return Collections.unmodifiableList(videoTrackPublications);
    }

    /** Returns read-only list of data track publications. */
    @NonNull
    @Override
    public List<DataTrackPublication> getDataTracks() {
        return Collections.unmodifiableList(dataTrackPublications);
    }

    /** Returns read-only list of remote audio track publications. */
    public List<RemoteAudioTrackPublication> getRemoteAudioTracks() {
        return Collections.unmodifiableList(remoteAudioTrackPublications);
    }

    /** Returns read-only list of remote video track publications. */
    public List<RemoteVideoTrackPublication> getRemoteVideoTracks() {
        return Collections.unmodifiableList(remoteVideoTrackPublications);
    }

    /** Returns a read-only list of remote data track publications. */
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
        if (isReleased()) {
            return false;
        } else {
            return nativeIsConnected(nativeParticipantContext);
        }
    }

    synchronized void release() {
        if (!isReleased()) {
            // Release all audio tracks
            for (RemoteAudioTrackPublication remoteAudioTrackPublication :
                    remoteAudioTrackPublications) {
                RemoteAudioTrack remoteAudioTrack =
                        remoteAudioTrackPublication.getRemoteAudioTrack();

                if (remoteAudioTrack != null) {
                    remoteAudioTrack.release();
                }
            }

            for (RemoteVideoTrackPublication remoteVideoTrackPublication :
                    remoteVideoTrackPublications) {
                RemoteVideoTrack remoteVideoTrack =
                        remoteVideoTrackPublication.getRemoteVideoTrack();

                if (remoteVideoTrack != null) {
                    remoteVideoTrack.release();
                }
            }

            // Release native participant
            nativeRelease(nativeParticipantContext);
            nativeParticipantContext = 0;
        }
    }

    boolean isReleased() {
        return nativeParticipantContext == 0;
    }

    private void addAudioTracks(List<RemoteAudioTrackPublication> remoteAudioTracks) {
        audioTrackPublications.addAll(remoteAudioTracks);
    }

    private void addVideoTracks(List<RemoteVideoTrackPublication> remoteVideoTracks) {
        videoTrackPublications.addAll(remoteVideoTracks);
    }

    private void addDataTracks(List<RemoteDataTrackPublication> remoteDataTracks) {
        dataTrackPublications.addAll(remoteDataTracks);
    }

    /** Interface that provides {@link RemoteParticipant} events. */
    public interface Listener {
        /**
         * This method notifies the listener that a {@link RemoteParticipant} has published a {@link
         * RemoteAudioTrack} to this {@link Room}. The audio of the track is not audible until the
         * track has been subscribed to.
         *
         * @param remoteParticipant The participant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has unpublished a
         * {@link RemoteAudioTrack} from this {@link Room}.
         *
         * @param remoteParticipant The participant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener the {@link RemoteAudioTrack} of the {@link
         * RemoteParticipant} has been subscribed to. The audio track is audible after this
         * callback.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         * @param remoteAudioTrack The audio track subscribed to.
         */
        void onAudioTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that media negotiation for a {@link RemoteAudioTrack}
         * failed.
         *
         * @param remoteParticipant The remoteParticipant object associated with the audio track.
         * @param remoteAudioTrackPublication The audio track publication for which subscription
         *     failed.
         * @param twilioException Exception that describes failure.
         */
        void onAudioTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull TwilioException twilioException);

        /**
         * This method notifies the listener that the {@link RemoteAudioTrack} of the {@link
         * RemoteParticipant} has been unsubscribed from. The track is no longer audible after being
         * unsubscribed from the audio track.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         * @param remoteAudioTrack The audio track unsubscribed from.
         */
        void onAudioTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                @NonNull RemoteAudioTrack remoteAudioTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has published a {@link
         * RemoteVideoTrack} to this {@link Room}. Video frames will not begin flowing until the
         * video track has been subscribed to.
         *
         * @param remoteParticipant The participant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed a {@link
         * RemoteVideoTrack} from this {@link Room}.
         *
         * @param remoteParticipant The participant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication);

        /**
         * This method notifies the listener the {@link RemoteVideoTrack} of the {@link
         * RemoteParticipant} has been subscribed to. Video frames are now flowing and can be
         * rendered.
         *
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         * @param remoteVideoTrack The video track subscribed to.
         */
        void onVideoTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that media negotiation for a {@link RemoteVideoTrack}
         * failed.
         *
         * @param remoteParticipant The remoteParticipant object associated with the video track.
         * @param remoteVideoTrackPublication The video track publication for which subscription
         *     failed.
         * @param twilioException Exception that describes failure.
         */
        void onVideoTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull TwilioException twilioException);

        /**
         * This method notifies the listener that the {@link RemoteVideoTrack} of the {@link
         * RemoteParticipant} has been unsubscribed from. Video frames are no longer flowing. All
         * {@link VideoRenderer}s of the video track have been removed before receiving this
         * callback to prevent native memory leaks.
         *
         * @param remoteParticipant The remoteParticipant object associated with this video track.
         * @param remoteVideoTrackPublication The video track publication.
         * @param remoteVideoTrack The video track removed from this room.
         */
        void onVideoTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                @NonNull RemoteVideoTrack remoteVideoTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has published a {@link
         * RemoteDataTrack} to this {@link Room}.
         *
         * @param remoteParticipant The participant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         */
        void onDataTrackPublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} has removed a {@link
         * RemoteDataTrack} from this {@link Room}.
         *
         * @param remoteParticipant The participant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         */
        void onDataTrackUnpublished(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication);

        /**
         * This method notifies the listener the {@link RemoteDataTrack} of the {@link
         * RemoteParticipant} has been subscribed to. Data track messages can be now be received.
         *
         * @param remoteParticipant The remoteParticipant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         * @param remoteDataTrack The data track subscribed to.
         */
        void onDataTrackSubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull RemoteDataTrack remoteDataTrack);

        /**
         * This method notifies the listener that media negotiation for a {@link RemoteDataTrack}
         * failed.
         *
         * @param remoteParticipant The remoteParticipant object associated with the data track.
         * @param remoteDataTrackPublication The data track publication for which subscription
         *     failed.
         * @param twilioException Exception that describes failure.
         */
        void onDataTrackSubscriptionFailed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull TwilioException twilioException);

        /**
         * This method notifies the listener that the {@link RemoteDataTrack} of the {@link
         * RemoteParticipant} has been unsubscribed from. Data track messages will no longer be
         * received.
         *
         * @param remoteParticipant The remoteParticipant object associated with this data track.
         * @param remoteDataTrackPublication The data track publication.
         * @param remoteDataTrack The data track removed from this room.
         */
        void onDataTrackUnsubscribed(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                @NonNull RemoteDataTrack remoteDataTrack);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} audio track has been
         * enabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackEnabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} audio track has been
         * disabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteAudioTrackPublication The audio track publication.
         */
        void onAudioTrackDisabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} video track has been
         * enabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackEnabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication);

        /**
         * This method notifies the listener that a {@link RemoteParticipant} video track has been
         * disabled.
         *
         * @param remoteParticipant The remoteParticipant object associated with this audio track.
         * @param remoteVideoTrackPublication The video track publication.
         */
        void onVideoTrackDisabled(
                @NonNull RemoteParticipant remoteParticipant,
                @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication);
    }

    private native boolean nativeIsConnected(long nativeHandle);

    private native void nativeRelease(long nativeHandle);
}
