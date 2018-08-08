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
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** Represents the local participant of a {@link Room} you are connected to. */
public class LocalParticipant implements Participant {
    private static final Logger logger = Logger.getLogger(LocalParticipant.class);

    private long nativeLocalParticipantHandle;
    private final String sid;
    private final String identity;
    private final List<AudioTrackPublication> audioTrackPublications;
    private final List<LocalAudioTrackPublication> localAudioTrackPublications;
    private final List<VideoTrackPublication> videoTrackPublications;
    private final List<LocalVideoTrackPublication> localVideoTrackPublications;
    private final List<DataTrackPublication> dataTrackPublications;
    private final List<LocalDataTrackPublication> localDataTrackPublications;
    private final Handler handler;

    /*
     * We pass all native participant callbacks through the listener proxy and atomically
     * forward events to the developer listener.
     */
    private final AtomicReference<Listener> listenerReference = new AtomicReference<>(null);

    /*
     * The listener proxy is bound at JNI level.
     */
    @SuppressWarnings("unused")
    private final Listener localParticipantListenerProxy =
            new Listener() {
                @Override
                public void onAudioTrackPublished(
                        final LocalParticipant localParticipant,
                        final LocalAudioTrackPublication localAudioTrackPublication) {
                    checkPublishedCallback(
                            localParticipant, localAudioTrackPublication, "onAudioTrackPublished");
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    logger.d("onAudioTrackPublished");
                                    audioTrackPublications.add(localAudioTrackPublication);
                                    localAudioTrackPublications.add(localAudioTrackPublication);
                                    Listener listener = listenerReference.get();

                                    if (listener != null) {
                                        listener.onAudioTrackPublished(
                                                localParticipant, localAudioTrackPublication);
                                    }
                                }
                            });
                }

                @Override
                public void onAudioTrackPublicationFailed(
                        final LocalParticipant localParticipant,
                        final LocalAudioTrack localAudioTrack,
                        final TwilioException twilioException) {
                    checkPublicationFailedCallback(
                            localParticipant,
                            localAudioTrack,
                            twilioException,
                            "onAudioTrackPublicationFailed");

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    logger.d("onAudioTrackPublicationFailed");
                                    Listener listener = listenerReference.get();

                                    if (listener != null) {
                                        listener.onAudioTrackPublicationFailed(
                                                localParticipant, localAudioTrack, twilioException);
                                    }
                                }
                            });
                }

                @Override
                public void onVideoTrackPublished(
                        final LocalParticipant localParticipant,
                        final LocalVideoTrackPublication localVideoTrackPublication) {
                    checkPublishedCallback(
                            localParticipant, localVideoTrackPublication, "onVideoTrackPublished");
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    logger.d("onAudioTrackPublished");
                                    videoTrackPublications.add(localVideoTrackPublication);
                                    localVideoTrackPublications.add(localVideoTrackPublication);
                                    Listener listener = listenerReference.get();

                                    if (listener != null) {
                                        listener.onVideoTrackPublished(
                                                localParticipant, localVideoTrackPublication);
                                    }
                                }
                            });
                }

                @Override
                public void onVideoTrackPublicationFailed(
                        final LocalParticipant localParticipant,
                        final LocalVideoTrack localVideoTrack,
                        final TwilioException twilioException) {
                    checkPublicationFailedCallback(
                            localParticipant,
                            localVideoTrack,
                            twilioException,
                            "onVideoTrackPublicationFailed");

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    logger.d("onVideoTrackPublicationFailed");
                                    Listener listener = listenerReference.get();

                                    if (listener != null) {
                                        listener.onVideoTrackPublicationFailed(
                                                localParticipant, localVideoTrack, twilioException);
                                    }
                                }
                            });
                }

                @Override
                public void onDataTrackPublished(
                        final LocalParticipant localParticipant,
                        final LocalDataTrackPublication localDataTrackPublication) {
                    checkPublishedCallback(
                            localParticipant, localDataTrackPublication, "onDataTrackPublished");
                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    logger.d("onDataTrackPublished");
                                    dataTrackPublications.add(localDataTrackPublication);
                                    localDataTrackPublications.add(localDataTrackPublication);
                                    Listener listener = listenerReference.get();

                                    if (listener != null) {
                                        listener.onDataTrackPublished(
                                                localParticipant, localDataTrackPublication);
                                    }
                                }
                            });
                }

                @Override
                public void onDataTrackPublicationFailed(
                        final LocalParticipant localParticipant,
                        final LocalDataTrack localDataTrack,
                        final TwilioException twilioException) {
                    checkPublicationFailedCallback(
                            localParticipant,
                            localDataTrack,
                            twilioException,
                            "onDataTrackPublicationFailed");

                    handler.post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    logger.d("onDataTrackPublicationFailed");
                                    Listener listener = listenerReference.get();

                                    if (listener != null) {
                                        listener.onDataTrackPublicationFailed(
                                                localParticipant, localDataTrack, twilioException);
                                    }
                                }
                            });
                }

                private void checkPublishedCallback(
                        LocalParticipant localParticipant,
                        TrackPublication trackPublication,
                        String callback) {
                    Preconditions.checkState(
                            localParticipant != null,
                            "Received null local participant in %s",
                            callback);
                    Preconditions.checkState(
                            trackPublication != null,
                            "Received null track publication in %s",
                            callback);
                }

                private void checkPublicationFailedCallback(
                        LocalParticipant localParticipant,
                        Track track,
                        TwilioException twilioException,
                        String callback) {
                    Preconditions.checkState(
                            localParticipant != null,
                            "Received null local participant in %s",
                            callback);
                    Preconditions.checkState(track != null, "Received null track in %s", callback);
                    Preconditions.checkState(
                            twilioException != null, "Received null exception in %s", callback);
                }
            };

    /** Returns the SID of the local participant. */
    @Override
    public String getSid() {
        return sid;
    }

    /** Returns the identity of the local participant. */
    @Override
    public String getIdentity() {
        return identity;
    }

    /** Returns read-only list of audio track publications. */
    @Override
    public synchronized List<AudioTrackPublication> getAudioTracks() {
        return Collections.unmodifiableList(audioTrackPublications);
    }

    /** Returns read-only list of video track publications. */
    @Override
    public synchronized List<VideoTrackPublication> getVideoTracks() {
        return Collections.unmodifiableList(videoTrackPublications);
    }

    /** Returns read-only list of data track publications. */
    @Override
    public synchronized List<DataTrackPublication> getDataTracks() {
        return Collections.unmodifiableList(dataTrackPublications);
    }

    /** Returns read-only list of local audio track publications. */
    public synchronized List<LocalAudioTrackPublication> getLocalAudioTracks() {
        return Collections.unmodifiableList(localAudioTrackPublications);
    }

    /** Returns read-only list of local video track publications. */
    public synchronized List<LocalVideoTrackPublication> getLocalVideoTracks() {
        return Collections.unmodifiableList(localVideoTrackPublications);
    }

    /** Returns read-only list of local data track publications. */
    public synchronized List<LocalDataTrackPublication> getLocalDataTracks() {
        return Collections.unmodifiableList(localDataTrackPublications);
    }

    /**
     * Shares audio track to all participants in a {@link Room}.
     *
     * @return true if the audio track published or false if the local participant is not connected
     *     or the track was already published.
     */
    public synchronized boolean publishTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        Preconditions.checkArgument(
                !localAudioTrack.isReleased(), "LocalAudioTrack must not be released");

        return !isReleased()
                && nativePublishAudioTrack(
                        nativeLocalParticipantHandle,
                        localAudioTrack,
                        localAudioTrack.getNativeHandle());
    }

    /**
     * Shares video track to all participants in a {@link Room}.
     *
     * @return true if the video track was published or false if the local participant is not
     *     connected or the track was already published.
     */
    public synchronized boolean publishTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        Preconditions.checkArgument(
                !localVideoTrack.isReleased(), "LocalVideoTrack must not be released");

        return !isReleased()
                && nativePublishVideoTrack(
                        nativeLocalParticipantHandle,
                        localVideoTrack,
                        localVideoTrack.getNativeHandle());
    }

    /**
     * Shared data track to all participants in a {@link Room}.
     *
     * @return true if the data track was published or false if the local participant is not
     *     connected or the track was already published.
     */
    public synchronized boolean publishTrack(@NonNull LocalDataTrack localDataTrack) {
        Preconditions.checkNotNull(localDataTrack, "LocalDataTrack must not be null");
        Preconditions.checkArgument(
                !localDataTrack.isReleased(), "LocalDataTrack must not be released");

        return !isReleased()
                && nativePublishDataTrack(
                        nativeLocalParticipantHandle,
                        localDataTrack,
                        localDataTrack.getNativeHandle());
    }

    /**
     * Stops the sharing of an audio track to all the participants in a {@link Room}.
     *
     * @return true if the audio track was unpublished or false if the local participant is not
     *     connected or could not unpublish audio track.
     */
    public synchronized boolean unpublishTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        Preconditions.checkArgument(
                !localAudioTrack.isReleased(), "LocalAudioTrack must not be released");
        if (isReleased()) {
            return false;
        } else {
            removePublishedAudioTrack(localAudioTrack);
            return nativeUnpublishAudioTrack(
                    nativeLocalParticipantHandle, localAudioTrack.getNativeHandle());
        }
    }

    /**
     * Stops the sharing of a video track to all the participants in a {@link Room}.
     *
     * @return true if video track was unpublished or false if the local participant is not
     *     connected or could not unpublish video track.
     */
    public synchronized boolean unpublishTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        Preconditions.checkArgument(
                !localVideoTrack.isReleased(), "LocalVideoTrack must not be released");
        if (isReleased()) {
            return false;
        } else {
            removePublishedVideoTrack(localVideoTrack);
            return nativeUnpublishVideoTrack(
                    nativeLocalParticipantHandle, localVideoTrack.getNativeHandle());
        }
    }

    /**
     * Stops the sharing of a data track to all the participants in a {@link Room}.
     *
     * @return true if the data track was unpublished or false if the local participant is not
     *     connected or could not unpublish the data track.
     */
    public synchronized boolean unpublishTrack(@NonNull LocalDataTrack localDataTrack) {
        Preconditions.checkNotNull(localDataTrack, "LocalDataTrack must not be null");
        Preconditions.checkArgument(
                !localDataTrack.isReleased(), "LocalDataTrack must not be released");
        if (isReleased()) {
            return false;
        } else {
            removePublishedDataTrack(localDataTrack);
            return nativeUnpublishDataTrack(
                    nativeLocalParticipantHandle, localDataTrack.getNativeHandle());
        }
    }

    /**
     * Set listener for local participant events.
     *
     * @param listener of local participant events.
     */
    public void setListener(@NonNull LocalParticipant.Listener listener) {
        Preconditions.checkNotNull(listener, "Listener must not be null");

        this.listenerReference.set(listener);
    }

    /**
     * Updates the {@link EncodingParameters} used to share media in the Room.
     *
     * @param encodingParameters The {@link EncodingParameters} to use or {@code null} for the
     *     default values.
     */
    public synchronized void setEncodingParameters(
            @Nullable EncodingParameters encodingParameters) {
        if (!isReleased()) {
            nativeSetEncodingParameters(nativeLocalParticipantHandle, encodingParameters);
        } else {
            logger.w("Cannot set encoding parameters after disconnected from a room");
        }
    }

    LocalParticipant(
            long nativeLocalParticipantHandle,
            @NonNull String sid,
            @NonNull String identity,
            @NonNull List<LocalAudioTrackPublication> localAudioTrackPublications,
            @NonNull List<LocalVideoTrackPublication> localVideoTrackPublications,
            @NonNull List<LocalDataTrackPublication> localDataTrackPublications,
            @NonNull Handler handler) {
        Preconditions.checkNotNull(sid, "SID must not be null");
        Preconditions.checkArgument(!sid.isEmpty(), "SID must not be empty");
        Preconditions.checkNotNull(identity, "Identity must not be null");
        this.nativeLocalParticipantHandle = nativeLocalParticipantHandle;
        this.sid = sid;
        this.identity = identity;
        this.localAudioTrackPublications = localAudioTrackPublications;
        this.audioTrackPublications = new ArrayList<>(localAudioTrackPublications.size());
        addAudioTracks(localAudioTrackPublications);
        this.localVideoTrackPublications = localVideoTrackPublications;
        this.videoTrackPublications = new ArrayList<>(localVideoTrackPublications.size());
        addVideoTracks(localVideoTrackPublications);
        this.localDataTrackPublications = localDataTrackPublications;
        this.dataTrackPublications = new ArrayList<>(localDataTrackPublications.size());
        addDataTracks(localDataTrackPublications);
        this.handler = handler;
    }

    /*
     * Releases native memory owned by local participant.
     */
    synchronized void release() {
        if (!isReleased()) {
            nativeRelease(nativeLocalParticipantHandle);
            nativeLocalParticipantHandle = 0;
        }
    }

    boolean isReleased() {
        return nativeLocalParticipantHandle == 0;
    }

    private void addAudioTracks(List<LocalAudioTrackPublication> localAudioTrackPublications) {
        this.audioTrackPublications.addAll(localAudioTrackPublications);
    }

    private void addVideoTracks(List<LocalVideoTrackPublication> localVideoTrackPublications) {
        this.videoTrackPublications.addAll(localVideoTrackPublications);
    }

    private void addDataTracks(List<LocalDataTrackPublication> localDataTrackPublications) {
        this.dataTrackPublications.addAll(localDataTrackPublications);
    }

    private void removePublishedAudioTrack(LocalAudioTrack localAudioTrack) {
        for (LocalAudioTrackPublication localAudioTrackPublication : localAudioTrackPublications) {
            if (localAudioTrack.equals(localAudioTrackPublication.getLocalAudioTrack())) {
                audioTrackPublications.remove(localAudioTrackPublication);
                localAudioTrackPublications.remove(localAudioTrackPublication);
                return;
            }
        }
    }

    private void removePublishedVideoTrack(LocalVideoTrack localVideoTrack) {
        for (LocalVideoTrackPublication localVideoTrackPublication : localVideoTrackPublications) {
            if (localVideoTrack.equals(localVideoTrackPublication.getLocalVideoTrack())) {
                videoTrackPublications.remove(localVideoTrackPublication);
                localVideoTrackPublications.remove(localVideoTrackPublication);
                return;
            }
        }
    }

    private void removePublishedDataTrack(LocalDataTrack localDataTrack) {
        for (LocalDataTrackPublication localDataTrackPublication : localDataTrackPublications) {
            if (localDataTrack.equals(localDataTrackPublication.getLocalDataTrack())) {
                dataTrackPublications.remove(localDataTrackPublication);
                localDataTrackPublications.remove(localDataTrackPublication);
                return;
            }
        }
    }

    /** Interface that provides {@link LocalParticipant} events. */
    public interface Listener {
        /**
         * This method notifies the listener that a {@link LocalAudioTrack} has been shared to a
         * {@link Room}.
         *
         * @param localParticipant The local participant that published the audio track.
         * @param localAudioTrackPublication The published local audio track.
         */
        void onAudioTrackPublished(
                LocalParticipant localParticipant,
                LocalAudioTrackPublication localAudioTrackPublication);

        /**
         * This method notifies the listener that the {@link LocalParticipant} failed to publish a
         * {@link LocalAudioTrack} to a {@link Room}.
         *
         * @param localParticipant The local participant that failed to publish the audio track.
         * @param localAudioTrack The local audio track that could not be published.
         * @param twilioException An exception explaining why the local participant failed to
         *     publish the local audio track.
         */
        void onAudioTrackPublicationFailed(
                LocalParticipant localParticipant,
                LocalAudioTrack localAudioTrack,
                TwilioException twilioException);

        /**
         * This method notifies the listener that a {@link LocalVideoTrack} has been shared to a
         * {@link Room}.
         *
         * @param localParticipant The local participant that published the video track.
         * @param localVideoTrackPublication The published local video track.
         */
        void onVideoTrackPublished(
                LocalParticipant localParticipant,
                LocalVideoTrackPublication localVideoTrackPublication);

        /**
         * This method notifies the listener that the {@link LocalParticipant} failed to publish a
         * {@link LocalVideoTrack} to a {@link Room}.
         *
         * @param localParticipant The local participant that failed to publish the video track.
         * @param localVideoTrack The local video track that could not be published.
         * @param twilioException An exception explaining why the local participant failed to
         *     publish the local video track.
         */
        void onVideoTrackPublicationFailed(
                LocalParticipant localParticipant,
                LocalVideoTrack localVideoTrack,
                TwilioException twilioException);

        /**
         * This method notifies the listener that a {@link LocalDataTrack} has been shared to a
         * {@link Room}.
         *
         * @param localParticipant The local participant that published the data track.
         * @param localDataTrackPublication The published local data track.
         */
        void onDataTrackPublished(
                LocalParticipant localParticipant,
                LocalDataTrackPublication localDataTrackPublication);

        /**
         * This method notifies the listener that the {@link LocalParticipant} failed to publish a
         * {@link LocalDataTrack} to a {@link Room}.
         *
         * @param localParticipant The local participant that failed to publish the data track.
         * @param localDataTrack The local data track that could not be published.
         * @param twilioException An exception explaining why the local participant failed to
         *     publish the local data track.
         */
        void onDataTrackPublicationFailed(
                LocalParticipant localParticipant,
                LocalDataTrack localDataTrack,
                TwilioException twilioException);
    }

    private native boolean nativePublishAudioTrack(
            long nativeHandle, LocalAudioTrack localAudioTrack, long nativeAudioTrackHandle);

    private native boolean nativePublishVideoTrack(
            long nativeHandle, LocalVideoTrack localVideoTrack, long nativeVideoTrackHandle);

    private native boolean nativePublishDataTrack(
            long nativeHandle, LocalDataTrack localDataTrack, long nativeDataTrackHandle);

    private native boolean nativeUnpublishAudioTrack(
            long nativeHandle, long nativeAudioTrackHandle);

    private native boolean nativeUnpublishVideoTrack(
            long nativeHandle, long nativeVideoTrackHandle);

    private native boolean nativeUnpublishDataTrack(long nativeHandle, long nativeDataTrackHandle);

    private native void nativeSetEncodingParameters(
            long nativeHandle, EncodingParameters encodingParameters);

    private native void nativeRelease(long nativeHandle);
}
