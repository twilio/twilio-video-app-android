package com.twilio.video;

public class LocalAudioTrack  {
    private org.webrtc.AudioTrack webrtcAudioTrack;

    LocalAudioTrack(org.webrtc.AudioTrack audioTrack) {
        webrtcAudioTrack = audioTrack;
    }

    public String getTrackId() {

        return webrtcAudioTrack.id();
    }

    public boolean isEnabled() {

        return webrtcAudioTrack.enabled();
    }

    public boolean enable(boolean enable) {

        return webrtcAudioTrack.setEnabled(enable);
    }
}
