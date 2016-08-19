package com.twilio.video;

/**
 * An audio track represents a single local or remote audio source
 */
public class AudioTrack {
    private org.webrtc.AudioTrack audioTrack;
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
        audioTrack = new org.webrtc.AudioTrack(nativeWebrtcTrack);
    }

    public String getTrackId() {
        return trackId;
    }

//    @Override
//    public MediaTrackState getState() {
//        return trackState;
//    }

    public boolean isEnabled() {
        return isEnabled;
    }
   
//    TrackInfo getTrackInfo() {
//        return trackInfo;
//    }
//
//    void updateTrackInfo(TrackInfo trackInfo) {
//        this.trackInfo = trackInfo;
//    }
//
//    void setTrackState(MediaTrackState trackState) {
//        this.trackState = trackState;
//    }
}
