package com.twilio.conversations.impl;

import com.twilio.conversations.AudioTrack;
import com.twilio.conversations.MediaTrackState;
import com.twilio.conversations.impl.core.TrackInfo;

public class AudioTrackImpl implements AudioTrack {
    private org.webrtc.AudioTrack audioTrack;
    private TrackInfo trackInfo;
    private MediaTrackState trackState;

    AudioTrackImpl(org.webrtc.AudioTrack audioTrack, TrackInfo trackInfo) {
        this.audioTrack = audioTrack;
        this.trackInfo = trackInfo;
        trackState = MediaTrackState.STARTED;
    }

    TrackInfo getTrackInfo() {
        return trackInfo;
    }

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
        if (audioTrack != null) {
            return audioTrack.enabled();
        }
        return false;
    }

    void updateTrackInfo(TrackInfo trackInfo) {
        this.trackInfo = trackInfo;
    }

    void setTrackState(MediaTrackState trackState) {
        this.trackState = trackState;
    }
}
