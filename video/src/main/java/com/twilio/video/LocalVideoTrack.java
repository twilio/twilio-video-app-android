package com.twilio.video;

/**
 * A local video track that gets camera video from a {@link CameraCapturer}
 *
 */
public class LocalVideoTrack  {
    private final org.webrtc.VideoTrack webRtcVideoTrack;
    private final VideoCapturer videoCapturer;
    private final VideoConstraints videoConstraints;
    private boolean enabledVideo = true;

    LocalVideoTrack(org.webrtc.VideoTrack webrtcVideoTrack,
                    VideoCapturer videoCapturer,
                    VideoConstraints videoConstraints) {
        this.webRtcVideoTrack = webrtcVideoTrack;
        this.videoCapturer = videoCapturer;
        this.videoConstraints = videoConstraints;
    }

    /**
     * Retrieves the {@link VideoCapturer} associated with this video track
     *
     * @return camera
     */
    public VideoCapturer getVideoCapturer() {
        return videoCapturer;
    }

    public boolean enable(boolean enabled) {
        if (webRtcVideoTrack != null) {
            enabledVideo = webRtcVideoTrack.setEnabled(enabled);
            if(enabledVideo && enabled) {
//                videoCapturer.resume();
            } else if(enabledVideo && !enabled){
//                videoCapturer.pause();
            }
        } else {
            enabledVideo = enabled;
        }
        return enabledVideo;
    }

    public String getTrackId() {
        return webRtcVideoTrack.id();
    }

    public boolean isEnabled() {
        return webRtcVideoTrack.enabled();
    }

    /**
     * Specifies the video constraints associated with this track
     *
     * If you do not provide any video constraints, the default video constraints are set to a
     * a minimum of 10 frames per second, a maximum of 30 frames per second, and a maximum video
     * dimension size of 640x480.
     *
     * @return video constraints
     */
    public VideoConstraints getVideoConstraints() {
        return videoConstraints;
    }
}