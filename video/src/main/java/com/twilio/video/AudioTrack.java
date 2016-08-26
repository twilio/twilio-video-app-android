package com.twilio.video;

/**
 * An audio track represents a single local or remote audio source
 */
public class AudioTrack {
    private org.webrtc.AudioTrack webrtcAudioTrack;
    private String trackId;
    private long nativeAudioTrackContext;
    private boolean isEnabled;

    AudioTrack(long nativeAudioTrackContext, String trackId,
               boolean isEnabled, long nativeWebrtcTrack) {
        this.nativeAudioTrackContext = nativeAudioTrackContext;
        this.trackId = trackId;
        this.isEnabled = isEnabled;
        webrtcAudioTrack = new org.webrtc.AudioTrack(nativeWebrtcTrack);
    }

    /**
     * This audio track id
     * @return track id
     */
    public String getTrackId() {
        return trackId;
    }

    /**
     * Check if this audio track is enabled
     * @return true if track is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    synchronized void release() {
        if (nativeAudioTrackContext != 0) {
            if (webrtcAudioTrack != null) {
                // TODO: Check if we are the one respnosible of disposing.
                // By looking at webrtc source code, RtpReceiver seems to be calling this method
                //webrtcAudioTrack.dispose();
                webrtcAudioTrack = null;
            }
            nativeRelease(nativeAudioTrackContext);
            nativeAudioTrackContext = 0;
        }
    }

    private native void nativeRelease(long nativeAudioTrackContext);

}
