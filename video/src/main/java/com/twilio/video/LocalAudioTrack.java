package com.twilio.video;

public class LocalAudioTrack extends AudioTrack {
    LocalAudioTrack(org.webrtc.AudioTrack audioTrack, TrackInfo trackInfo) {
        super(audioTrack, trackInfo);
    }

    @Override
    public String getTrackId() {
        return audioTrack.id();
    }

    @Override
    public boolean isEnabled() {
        return audioTrack.enabled();
    }

    public boolean enable(boolean enable) {
        return audioTrack.setEnabled(enable);
    }
}
