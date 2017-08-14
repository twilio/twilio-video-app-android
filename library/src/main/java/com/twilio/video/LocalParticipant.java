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
    private final List<AudioTrack> audioTracks;
    private final List<PublishedAudioTrack> publishedAudioTracks;
    private final List<VideoTrack> videoTracks;
    private final List<PublishedVideoTrack> publishedVideoTracks;
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
        public void onPublishedAudioTrack(final LocalParticipant localParticipant,
                                          final PublishedAudioTrack publishedAudioTrack) {
            checkCallback(localParticipant, publishedAudioTrack, "onPublishedAudioTrack");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onPublishedAudioTrack");
                    publishedAudioTracks.add(publishedAudioTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onPublishedAudioTrack(localParticipant, publishedAudioTrack);
                    }
                }
            });
        }

        @Override
        public void onPublishedVideoTrack(final LocalParticipant localParticipant,
                                          final PublishedVideoTrack publishedVideoTrack) {
            checkCallback(localParticipant, publishedVideoTrack, "onPublishedVideoTrack");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.d("onPublishedAudioTrack");
                    publishedVideoTracks.add(publishedVideoTrack);
                    Listener listener = listenerReference.get();

                    if (listener != null) {
                        listener.onPublishedVideoTrack(localParticipant, publishedVideoTrack);
                    }
                }
            });
        }

        private void checkCallback(LocalParticipant localParticipant,
                                   PublishedTrack track,
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
     * Returns read-only list of audio tracks.
     */
    @Override
    public synchronized List<AudioTrack> getAudioTracks() {
        return Collections.unmodifiableList(audioTracks);
    }

    /**
     * Returns read-only list of video tracks.
     */
    @Override
    public synchronized List<VideoTrack> getVideoTracks() {
        return Collections.unmodifiableList(videoTracks);
    }

    /**
     * Returns read-only list of published audio tracks.
     */
    public synchronized List<PublishedAudioTrack> getPublishedAudioTracks() {
        return Collections.unmodifiableList(publishedAudioTracks);
    }

    /**
     * Returns read-only list of published video tracks.
     */
    public synchronized List<PublishedVideoTrack> getPublishedVideoTracks() {
        return Collections.unmodifiableList(publishedVideoTracks);
    }

    /**
     * Shares audio track to all participants in a {@link Room}.
     *
     * @return true if the audio track published or false if the local participant is not connected
     * or the track was already published.
     */
    public synchronized boolean publishAudioTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            boolean added = nativePublishAudioTrack(nativeLocalParticipantHandle,
                    localAudioTrack.getNativeHandle());
            if (added) {
                audioTracks.add(localAudioTrack);
            }
            return added;
        }
    }

    /**
     * Shares video track to all participants in a {@link Room}.
     *
     * @return true if the video track was published or false if the local participant is
     * not connected or the track was already published.
     */
    public synchronized boolean publishVideoTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            boolean added = nativePublishVideoTrack(nativeLocalParticipantHandle,
                    localVideoTrack.getNativeHandle());
            if (added) {
                videoTracks.add(localVideoTrack);
            }
            return added;
        }
    }

    /**
     * Stops the sharing of an audio track to all the participants in a {@link Room}.
     *
     * @return true if the audio track was unpublished or false if the local participant is not
     * connected or could not unpublish audio track.
     */

    public synchronized boolean unpublishAudioTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            removePublishedAudioTrack(localAudioTrack);
            audioTracks.remove(localAudioTrack);
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
    public synchronized boolean unpublishVideoTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            removePublishedVideoTrack(localVideoTrack);
            videoTracks.remove(localVideoTrack);
            return nativeUnpublishVideoTrack(nativeLocalParticipantHandle,
                    localVideoTrack.getNativeHandle());
        }
    }

    /**
     * Set listener for local participant events.
     *
     * @param listener of local participant events.
     */
    public void setListener(LocalParticipant.Listener listener) {
        Preconditions.checkNotNull(listener, "Listener must not be null");

        this.listenerReference.set(listener);
    }

    LocalParticipant(long nativeLocalParticipantHandle,
                     @NonNull String sid,
                     @NonNull String identity,
                     @Nullable List<LocalAudioTrack> localAudioTracks,
                     @Nullable List<LocalVideoTrack> localVideoTracks,
                     @NonNull List<PublishedAudioTrack> publishedAudioTracks,
                     @NonNull List<PublishedVideoTrack> publishedVideoTracks,
                     @NonNull Handler handler) {
        Preconditions.checkNotNull(sid, "SID must not be null");
        Preconditions.checkArgument(!sid.isEmpty(), "SID must not be empty");
        Preconditions.checkNotNull(identity, "Identity must not be null");
        this.nativeLocalParticipantHandle = nativeLocalParticipantHandle;
        this.sid = sid;
        this.identity = identity;

        // Setup audio tracks
        if (localAudioTracks == null) {
            this.audioTracks = new ArrayList<>();
        } else {
            this.audioTracks = new ArrayList<>(localAudioTracks.size());
            addAudioTracks(localAudioTracks);
        }

        // Setup video tracks
        if (localVideoTracks == null) {
            this.videoTracks = new ArrayList<>();
        } else {
            this.videoTracks = new ArrayList<>(localVideoTracks.size());
            addVideoTracks(localVideoTracks);
        }

        this.publishedAudioTracks = publishedAudioTracks;
        this.publishedVideoTracks = publishedVideoTracks;
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

    private void addAudioTracks(List<LocalAudioTrack> localAudioTracks) {
        for (LocalAudioTrack localAudioTrack : localAudioTracks) {
            this.audioTracks.add(localAudioTrack);
        }
    }

    private void addVideoTracks(List<LocalVideoTrack> localVideoTracks) {
        for (LocalVideoTrack localVideoTrack : localVideoTracks) {
            this.videoTracks.add(localVideoTrack);
        }
    }

    private void removePublishedAudioTrack(LocalAudioTrack localAudioTrack) {
        for (PublishedAudioTrack publishedAudioTrack : publishedAudioTracks) {
            if (localAudioTrack.getTrackId().equals(publishedAudioTrack.getTrackId())) {
                publishedAudioTracks.remove(publishedAudioTrack);
                return;
            }
        }
    }

    private void removePublishedVideoTrack(LocalVideoTrack localVideoTrack) {
        for (PublishedVideoTrack publishedVideoTrack : publishedVideoTracks) {
            if (localVideoTrack.getTrackId().equals(publishedVideoTrack.getTrackId())) {
                publishedVideoTracks.remove(publishedVideoTrack);
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
         * @param publishedAudioTrack The published local audio track.
         */
        void onPublishedAudioTrack(LocalParticipant localParticipant,
                                   PublishedAudioTrack publishedAudioTrack);
        /**
         * This method notifies the listener that a {@link LocalVideoTrack} has been shared to a
         * {@link Room}.
         *
         * @param localParticipant The local participant that published the video track.
         * @param publishedVideoTrack The published local video track.
         */
        void onPublishedVideoTrack(LocalParticipant localParticipant,
                                   PublishedVideoTrack publishedVideoTrack);
    }

    private native boolean nativePublishAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native boolean nativePublishVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native boolean nativeUnpublishAudioTrack(long nativeHandle,
                                                     long nativeAudioTrackHandle);
    private native boolean nativeUnpublishVideoTrack(long nativeHandle,
                                                     long nativeVideoTrackHandle);
    private native void nativeRelease(long nativeHandle);
}
