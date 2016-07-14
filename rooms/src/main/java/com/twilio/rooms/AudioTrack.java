package com.twilio.rooms;

/**
 * An audio track represents a single local or remote audio source
 */
public class AudioTrack implements MediaTrack {
    private org.webrtc.AudioTrack audioTrack;
    private TrackInfo trackInfo;
    private MediaTrackState trackState;

    @Override
    public String getTrackId() {
        return trackInfo != null ? trackInfo.getTrackId() : null;
    }

    @Override
    public MediaTrackState getState() {
        return trackState;
    }

    @Override
    public boolean isEnabled() {
        if ((audioTrack != null) && (trackInfo != null)) {
            return trackInfo.isEnabled();
        }
        return false;
    }

    AudioTrack(org.webrtc.AudioTrack audioTrack, TrackInfo trackInfo) {
        this.audioTrack = audioTrack;
        this.trackInfo = trackInfo;
        trackState = MediaTrackState.STARTED;
    }

    TrackInfo getTrackInfo() {
        return trackInfo;
    }

    void updateTrackInfo(TrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }

    void setTrackState(MediaTrackState trackState) {
        this.trackState = trackState;
    }
}
