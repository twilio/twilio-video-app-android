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

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the local participant of a {@link Room} you are connected to.
 */
public class LocalParticipant implements Participant {
    private long nativeLocalParticipantHandle;
    private final String sid;
    private final String identity;
    private final List<AudioTrack> audioTracks;
    private final List<LocalAudioTrack> publishedAudioTracks;
    private final List<VideoTrack> videoTracks;
    private final List<LocalVideoTrack> publishedVideoTracks;

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
    public synchronized List<LocalAudioTrack> getPublishedAudioTracks() {
        return Collections.unmodifiableList(publishedAudioTracks);
    }

    /**
     * Returns read-only list of published video tracks.
     */
    public synchronized List<LocalVideoTrack> getPublishedVideoTracks() {
        return Collections.unmodifiableList(publishedVideoTracks);
    }

    /**
     * Adds an audio track to the local participant. If the local participant is connected to
     * {@link Room} then the audio track will be shared with all other participants.
     *
     * @return true if the audio track was added or false if the local participant is not connected
     * or the track was already added.
     */
    public synchronized boolean addAudioTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            boolean added = nativeAddAudioTrack(nativeLocalParticipantHandle,
                    localAudioTrack.getNativeHandle());
            if (added) {
                publishedAudioTracks.add(localAudioTrack);
                audioTracks.add(localAudioTrack);
            }
            return added;
        }
    }

    /**
     * Adds a video track to the local participant. If the local participant is connected to
     * {@link Room} then the video track will be shared with all other participants.
     *
     * @return true if the video track was added or false if the local participant is not connected
     * or the track was already added.
     */
    public synchronized boolean addVideoTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            boolean added = nativeAddVideoTrack(nativeLocalParticipantHandle,
                    localVideoTrack.getNativeHandle());
            if (added) {
                publishedVideoTracks.add(localVideoTrack);
                videoTracks.add(localVideoTrack);
            }
            return added;
        }
    }

    /**
     * Removes the audio track from the local participant. If the local participant is connected to
     * {@link Room} then the audio track will no longer be shared with other participants.
     *
     * @return true if the audio track was removed or false if the local participant is not connected
     * or could not remove audio track.
     */

    public synchronized boolean removeAudioTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            publishedAudioTracks.remove(localAudioTrack);
            audioTracks.remove(localAudioTrack);
            return nativeRemoveAudioTrack(nativeLocalParticipantHandle,
                    localAudioTrack.getNativeHandle());
        }
    }

    /**
     * Removes the video track from the local participant. If the local participant is connected to
     * {@link Room} then the video track will no longer be shared with other participants.
     *
     * @return true if video track was removed or false if the local participant is not connected
     * or could not remove video track.
     */
    public synchronized boolean removeVideoTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        if (isReleased()) {
            return false;
        } else {
            publishedVideoTracks.remove(localVideoTrack);
            videoTracks.remove(localVideoTrack);
            return nativeRemoveVideoTrack(nativeLocalParticipantHandle,
                    localVideoTrack.getNativeHandle());
        }
    }

    LocalParticipant(long nativeLocalParticipantHandle,
                     String sid,
                     String identity,
                     List<LocalAudioTrack> publishedAudioTracks,
                     List<LocalVideoTrack> publishedVideoTracks) {
        this.nativeLocalParticipantHandle = nativeLocalParticipantHandle;
        this.sid = sid;
        this.identity = identity;
        if (publishedAudioTracks == null) {
            publishedAudioTracks = new ArrayList<>();
        }
        this.publishedAudioTracks = publishedAudioTracks;
        this.audioTracks = new ArrayList<>(publishedAudioTracks.size());
        addAudioTracks(publishedAudioTracks);
        if (publishedVideoTracks == null) {
            publishedVideoTracks = new ArrayList<>();
        }
        this.publishedVideoTracks = publishedVideoTracks;
        this.videoTracks = new ArrayList<>(publishedVideoTracks.size());
        addVideoTracks(publishedVideoTracks);
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

    private native boolean nativeAddAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native boolean nativeAddVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native boolean nativeRemoveAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native boolean nativeRemoveVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native void nativeRelease(long nativeHandle);
}
