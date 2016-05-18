package com.twilio.conversations;

/**
 * A local video track that gets camera video from a {@link CameraCapturer}
 *
 */
public class LocalVideoTrack extends VideoTrack {
    private VideoConstraints videoConstraints;
    private CameraCapturer cameraCapturer;
    private boolean enabledVideo = true;

    public LocalVideoTrack(CameraCapturer cameraCapturer) {
        super();
        if(cameraCapturer == null) {
            throw new NullPointerException("CameraCapturer must not be null");
        }
        this.cameraCapturer = cameraCapturer;
        this.videoConstraints = defaultVideoConstraints();
    }

    public LocalVideoTrack(CameraCapturer cameraCapturer, VideoConstraints videoConstraints) {
        super();
        if(cameraCapturer == null) {
            throw new NullPointerException("CameraCapturer must not be null");
        }
        if(videoConstraints == null) {
            throw new NullPointerException("VideoConstraints must not be null");
        }
        this.cameraCapturer = cameraCapturer;
        this.videoConstraints = videoConstraints;
    }

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

    /**
     * Retrieves the {@link CameraCapturer} associated with this video track
     *
     * @return camera
     */
    public CameraCapturer getCameraCapturer() {
        return cameraCapturer;
    }

    /**
     * Specifies whether or not your camera video should be shared
     *
     * @param enabled <code>true</code> if camera should be shared, false otherwise
     * @return true if the operation succeeded. false if there is an operation in progress.
     */
    public boolean enable(boolean enabled) {
        org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
        if (videoTrack != null) {
            enabledVideo = videoTrack.setEnabled(enabled);
            if(enabledVideo && enabled) {
                cameraCapturer.resume();
            } else if(enabledVideo && !enabled){
                cameraCapturer.pause();
            }
        } else {
            enabledVideo = enabled;
        }
        return enabledVideo;
    }

    @Override
    public boolean isEnabled() {
        org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
        if (videoTrack != null) {
            return videoTrack.enabled();
        } else {
            return enabledVideo;
        }
    }

    void removeCameraCapturer() {
        cameraCapturer.resetNativeVideoCapturer();
        cameraCapturer = null;
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