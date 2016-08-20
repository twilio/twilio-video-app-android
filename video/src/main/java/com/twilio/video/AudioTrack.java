package com.twilio.video;

/**
 * An audio track represents a single local or remote audio source
 */
public class AudioTrack {
    private org.webrtc.AudioTrack webrtcAudioTrack;
    //private TrackInfo trackInfo;
    private String trackId;
    private MediaTrackState trackState;
    private long nativeAudioTrackContext;
    private boolean isEnabled;

    AudioTrack(long nativeAudioTrackContext, String trackId,
               boolean isEnabled, long nativeWebrtcTrack) {
        this.nativeAudioTrackContext = nativeAudioTrackContext;
        this.trackId = trackId;
        this.isEnabled = isEnabled;
        webrtcAudioTrack = new org.webrtc.AudioTrack(nativeWebrtcTrack);
    }

    public String getTrackId() {
        return trackId;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    void release() {
        if (nativeAudioTrackContext != 0) {
            if (webrtcAudioTrack != null) {
                webrtcAudioTrack.dispose();
                webrtcAudioTrack = null;
            }
            nativeRelease(nativeAudioTrackContext);
            nativeAudioTrackContext = 0;
        }
    }

    private native void nativeRelease(long nativeAudioTrackContext);

}
