package com.twilio.video;

public class LocalAudioTrack extends AudioTrack {
    LocalAudioTrack(org.webrtc.AudioTrack audioTrack, TrackInfo trackInfo) {
        super(audioTrack, trackInfo);
    }

    @Override
    public String getTrackId() {
        org.webrtc.AudioTrack audioTrack = getWebrtcTrack();

        return audioTrack.id();
    }

    @Override
    public boolean isEnabled() {
        org.webrtc.AudioTrack audioTrack = getWebrtcTrack();

        return audioTrack.enabled();
    }

    public boolean enable(boolean enable) {
        org.webrtc.AudioTrack audioTrack = getWebrtcTrack();

        return audioTrack.setEnabled(enable);
    }
}
