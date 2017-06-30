package com.twilio.video;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the local participant of a {@link Room} you are connected to.
 */
public class LocalParticipant {
    private long nativeLocalParticipantHandle;
    private final String sid;
    private final String identity;
    private final List<LocalAudioTrack> audioTracks;
    private final List<LocalVideoTrack> videoTracks;

    /**
     * Returns the SID of the local participant.
     */
    public String getSid() {
        return sid;
    }

    /**
     * Returns the identity of the local participant.
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the {@link LocalAudioTrack}s of a local participant.
     */
    public synchronized List<LocalAudioTrack> getAudioTracks() {
        return new ArrayList<>(audioTracks);
    }

    /**
     * Returns the {@link LocalVideoTrack}s of a local participant.
     */
    public synchronized List<LocalVideoTrack> getVideoTracks() {
        return new ArrayList<>(videoTracks);
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
            boolean added = nativeAddAudioTrack(nativeLocalParticipantHandle, localAudioTrack.getNativeHandle());
            if (added) {
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
            boolean added = nativeAddVideoTrack(nativeLocalParticipantHandle, localVideoTrack.getNativeHandle());
            if (added) {
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
            audioTracks.remove(localAudioTrack);
            return nativeRemoveAudioTrack(nativeLocalParticipantHandle, localAudioTrack.getNativeHandle());
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
            videoTracks.remove(localVideoTrack);
            return nativeRemoveVideoTrack(nativeLocalParticipantHandle, localVideoTrack.getNativeHandle());
        }
    }

    LocalParticipant(long nativeLocalParticipantHandle,
                     String sid,
                     String identity,
                     List<LocalAudioTrack> audioTracks,
                     List<LocalVideoTrack> videoTracks) {
        this.nativeLocalParticipantHandle = nativeLocalParticipantHandle;
        this.sid = sid;
        this.identity = identity;
        if (audioTracks == null) {
            audioTracks = new ArrayList<>();
        }
        this.audioTracks = audioTracks;
        if (videoTracks == null) {
            videoTracks = new ArrayList<>();
        }
        this.videoTracks = videoTracks;
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

    private native boolean nativeAddAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native boolean nativeAddVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native boolean nativeRemoveAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native boolean nativeRemoveVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native void nativeRelease(long nativeHandle);
}
