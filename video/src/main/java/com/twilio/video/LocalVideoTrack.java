package com.twilio.video;

/**
 * A local video track that gets camera video from a {@link CameraCapturer}
 *
 */
public class LocalVideoTrack extends VideoTrack {
    private VideoConstraints videoConstraints;
    private VideoCapturer videoCapturer;
    private boolean enabledVideo = true;

    /**
     * TODO
     * Obtain the default video constraints using JNI
     * to ensure we stay in sync if the defaults change in the core.
     * This requires a significant amount of boilerplate with the current
     * implementation but it can be greatly simplified with a small core refactor.
     */
    private static VideoConstraints defaultVideoConstraints() {
        return new VideoConstraints.Builder()
                .minFps(10)
                .maxFps(30)
                .aspectRatio(VideoConstraints.ASPECT_RATIO_4_3)
                .maxVideoDimensions(new VideoDimensions(640,480))
                .build();
    }

    LocalVideoTrack(org.webrtc.VideoTrack rtcTrack,
                    VideoCapturer videoCapturer,
                    VideoConstraints videoConstraints) {
        super(rtcTrack);
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
        org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
        if (videoTrack != null) {
            enabledVideo = videoTrack.setEnabled(enabled);
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

    @Override
    public String getTrackId() {
        org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();

        return videoTrack.id();
    }

    @Override
    public boolean isEnabled() {
        org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();

        return videoTrack.enabled();
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