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
    public List<LocalAudioTrack> getAudioTracks() {
        return new ArrayList<>(audioTracks);
    }

    /**
     * Returns the {@link LocalVideoTrack}s of a local participant.
     */
    public List<LocalVideoTrack> getVideoTracks() {
        return new ArrayList<>(videoTracks);
    }

    /**
     * Adds an audio track to the local participant. If the local participant is connected to
     * {@link Room} then the audio track will be shared with all other participants.
     */
    public void addAudioTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        audioTracks.add(localAudioTrack);
        nativeAddAudioTrack(nativeLocalParticipantHandle, localAudioTrack.getNativeHandle());
    }

    /**
     * Adds a video track to the local participant. If the local participant is connected to
     * {@link Room} then the video track will be shared with all other participants.
     */
    public void addVideoTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        videoTracks.add(localVideoTrack);
        nativeAddVideoTrack(nativeLocalParticipantHandle, localVideoTrack.getNativeHandle());
    }

    /**
     * Removes the audio track from the local participant. If the local participant is connected to
     * {@link Room} then the audio track will no longer be shared with other participants.
     *
     * @return true if the audio track was removed or false if the local participant is not connected
     * or could not remove audio track.
     */

    public boolean removeAudioTrack(@NonNull LocalAudioTrack localAudioTrack) {
        Preconditions.checkNotNull(localAudioTrack, "LocalAudioTrack must not be null");
        nativeRemoveAudioTrack(nativeLocalParticipantHandle, localAudioTrack.getNativeHandle());
        return audioTracks.remove(localAudioTrack);
    }

    /**
     * Removes the video track from the local participant. If the local participant is connected to
     * {@link Room} then the video track will no longer be shared with other participants.
     *
     * @return true if video track was removed or false if the local participant is not connected
     * or could not remove video track.
     */
    public boolean removeVideoTrack(@NonNull LocalVideoTrack localVideoTrack) {
        Preconditions.checkNotNull(localVideoTrack, "LocalVideoTrack must not be null");
        nativeRemoveVideoTrack(nativeLocalParticipantHandle, localVideoTrack.getNativeHandle());
        return videoTracks.remove(localVideoTrack);
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

    /**
     * Releases native memory owned by local participant.
     */
    public synchronized void release() {
        if (!isReleased()) {
            nativeRelease(nativeLocalParticipantHandle);
            nativeLocalParticipantHandle = 0;
        }
    }

    boolean isReleased() {
        return nativeLocalParticipantHandle == 0;
    }

    private native void nativeAddAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native void nativeAddVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native void nativeRemoveAudioTrack(long nativeHandle, long nativeAudioTrackHandle);
    private native void nativeRemoveVideoTrack(long nativeHandle, long nativeVideoTrackHandle);
    private native void nativeRelease(long nativeHandle);

}
