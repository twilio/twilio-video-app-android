package com.twilio.video;

public class LocalAudioTrack extends AudioTrack  {

    LocalAudioTrack(org.webrtc.AudioTrack webrtcAudioTrack) {
        super(webrtcAudioTrack);
    }

    public String getTrackId() {
        org.webrtc.AudioTrack webrtcAudioTrack = getWebrtcAudioTrack();

        return webrtcAudioTrack.id();
    }

    public boolean isEnabled() {
        org.webrtc.AudioTrack webrtcAudioTrack = getWebrtcAudioTrack();

        return webrtcAudioTrack.enabled();
    }

    public boolean enable(boolean enable) {
        org.webrtc.AudioTrack webrtcAudioTrack = getWebrtcAudioTrack();

        return webrtcAudioTrack.setEnabled(enable);
    }
}
