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

/**
 * Represents the local participant of a {@link Room} you are connected to.
 */
public class LocalParticipant implements Participant {
    private static final Logger logger = Logger.getLogger(LocalParticipant.class);

    private long nativeLocalParticipantHandle;
    private final String sid;
    private final String identity;
    private final List<AudioTrackPublication> audioTrackPublications;
    private final List<LocalAudioTrackPublication> localAudioTrackPublications;
    private final List<VideoTrackPublication> videoTrackPublications;
    private final List<LocalVideoTrackPublication> localVideoTrackPublications;
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
    private final Listener localParticipantListenerProxy = new Listener() {
        @Override
        public void onAudioTrackPublished(final LocalParticipant localParticipant,
                                          final LocalAudioTrackPublication localAudioTrackPublication) {
            checkCallback(localParticipant, localAudioTrackPublication, "onAudioTrackPublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackPublished");
                    audioTrackPublications.add(localAudioTrackPublication);
                    localAudioTrackPublications.add(localAudioTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onAudioTrackPublished(localParticipant, localAudioTrackPublication);
                    }
                }
            });
        }

        @Override
        public void onVideoTrackPublished(final LocalParticipant localParticipant,
                                          final LocalVideoTrackPublication localVideoTrackPublication) {
            checkCallback(localParticipant, localVideoTrackPublication, "onVideoTrackPublished");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onAudioTrackPublished");
                    videoTrackPublications.add(localVideoTrackPublication);
                    localVideoTrackPublications.add(localVideoTrackPublication);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onVideoTrackPublished(localParticipant, localVideoTrackPublication);
                    }
                }
            });
        }

        private void checkCallback(LocalParticipant localParticipant,
                                   TrackPublication track,
                                   String callback) {
            Preconditions.checkState(localParticipant != null, "Received null local participant " +
                    "in %s", callback);
            Preconditions.checkState(track != null, "Received null track in %s", callback);
        }
    };

    /**
     * Returns the SID of the local participant.
     */
    @Override
    public String getSid() {
        return sid;
    }

    /**
     * Returns the identity of the local participant.
     */
    @Override
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns read-only list of audio track publications.
     */
    @Override
    public synchronized List<AudioTrackPublication> getAudioTracks() {
        return Collections.unmodifiableList(audioTrackPublications);
    }

    /**
     * Returns read-only list of video track publications.
     */
    @Override
    public synchronized List<VideoTrackPublication> getVideoTracks() {
        return Collections.unmodifiableList(videoTrackPublications);
    }

    /**
     * Returns read-only list of local audio track publications.
     */
    public synchronized List<LocalAudioTrackPublication> getLocalAudioTracks() {
        return Collections.unmodifiableList(localAudioTrackPublications);
    }

    /**
     * Returns read-only list of local video track publications.
     */
    public synchronized List<LocalVideoTrackPublication> getLocalVideoTracks() {
        return Collections.unmodifiableList(localVideoTrackPublications);
    }

    /**
     * Shares audio track to all participants in a {@link Room}.
     *
     * @return true if the audio track published or false if the local participant is not connected
     * or the track was already published.
     */
    public synchronized boolean publishTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            return nativePublishAudioTrack(nativeLocalParticipantHandle,
                    localAudioTrack,
                    localAudioTrack.getNativeHandle());
        }
    }

    /**
     * Shares video track to all participants in a {@link Room}.
     *
     * @return true if the video track was published or false if the local participant is
     * not connected or the track was already published.
     */
    public synchronized boolean publishTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            return nativePublishVideoTrack(nativeLocalParticipantHandle,
                    localVideoTrack,
                    localVideoTrack.getNativeHandle());
        }
    }

    /**
     * Stops the sharing of an audio track to all the participants in a {@link Room}.
     *
     * @return true if the audio track was unpublished or false if the local participant is not
     * connected or could not unpublish audio track.
     */

    public synchronized boolean unpublishTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            removePublishedAudioTrack(localAudioTrack);
            return nativeUnpublishAudioTrack(nativeLocalParticipantHandle,
                    localAudioTrack.getNativeHandle());
        }
    }

    /**
     * Stops the sharing of a video track to all the participants in a {@link Room}.
     *
     * @return true if video track was unpublished or false if the local participant is not
     * connected or could not unpublish video track.
     */
    public synchronized boolean unpublishTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            removePublishedVideoTrack(localVideoTrack);
            return nativeUnpublishVideoTrack(nativeLocalParticipantHandle,
                    localVideoTrack.getNativeHandle());
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
     *  Updates the {@link EncodingParameters} used to share media in the Room.
     *
     *  @param encodingParameters The {@link EncodingParameters} to use or {@code null} for the
     *                            default values.
     */
    public synchronized void setEncodingParameters(@Nullable EncodingParameters encodingParameters) {
        if (!isReleased()) {
            nativeSetEncodingParameters(nativeLocalParticipantHandle, encodingParameters);
        } else {
            logger.w("Cannot set encoding parameters after disconnected from a room");
        }
    }

    LocalParticipant(long nativeLocalParticipantHandle,
                     @NonNull String sid,
                     @NonNull String identity,
                     @NonNull List<LocalAudioTrackPublication> localAudioTrackPublications,
                     @NonNull List<LocalVideoTrackPublication> localVideoTrackPublications,
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
        for (LocalAudioTrackPublication localAudioTrackPublication : localAudioTrackPublications) {
            this.audioTrackPublications.add(localAudioTrackPublication);
        }
    }

    private void addVideoTracks(List<LocalVideoTrackPublication> localVideoTrackPublications) {
        for (LocalVideoTrackPublication localVideoTrackPublication : localVideoTrackPublications) {
            this.videoTrackPublications.add(localVideoTrackPublication);
        }
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

    /**
     * Interface that provides {@link LocalParticipant} events.
     */
    public interface Listener {
        /**
         * This method notifies the listener that a {@link LocalAudioTrack} has been shared to a
         * {@link Room}.
         *
         * @param localParticipant The local participant that published the audio track.
         * @param localAudioTrackPublication The published local audio track.
         */
        void onAudioTrackPublished(LocalParticipant localParticipant,
                                   LocalAudioTrackPublication localAudioTrackPublication);
        /**
         * This method notifies the listener that a {@link LocalVideoTrack} has been shared to a
         * {@link Room}.
         *
         * @param localParticipant The local participant that published the video track.
         * @param localVideoTrackPublication The published local video track.
         */
        void onVideoTrackPublished(LocalParticipant localParticipant,
                                   LocalVideoTrackPublication localVideoTrackPublication);
    }

    private native boolean nativePublishAudioTrack(long nativeHandle,
                                                   LocalAudioTrack localAudioTrack,
                                                   long nativeAudioTrackHandle);
    private native boolean nativePublishVideoTrack(long nativeHandle,
                                                   LocalVideoTrack localVideoTrack,
                                                   long nativeVideoTrackHandle);
    private native boolean nativeUnpublishAudioTrack(long nativeHandle,
                                                     long nativeAudioTrackHandle);
    private native boolean nativeUnpublishVideoTrack(long nativeHandle,
                                                     long nativeVideoTrackHandle);
    private native void nativeSetEncodingParameters(long nativeHandle,
                                                    EncodingParameters encodingParameters);
    private native void nativeRelease(long nativeHandle);
}
