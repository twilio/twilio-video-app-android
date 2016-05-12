package com.twilio.conversations.impl;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.VideoConstraints;
import com.twilio.conversations.VideoDimensions;

public class LocalVideoTrackImpl extends VideoTrackImpl implements LocalVideoTrack  {
    private VideoConstraints videoConstraints;
    private CameraCapturer cameraCapturer;
    private boolean enabledVideo = true;

    public LocalVideoTrackImpl(CameraCapturer cameraCapturer) {
        super();
        if(cameraCapturer == null) {
            throw new NullPointerException("CameraCapturer must not be null");
        }
        this.cameraCapturer = cameraCapturer;
        this.videoConstraints = defaultVideoConstraints();
    }

    public LocalVideoTrackImpl(CameraCapturer cameraCapturer, VideoConstraints videoConstraints) {
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

    @Override
    public CameraCapturer getCameraCapturer() {
        return cameraCapturer;
    }

    @Override
    public boolean enable(boolean enabled) {
        org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
        if (videoTrack != null) {
            enabledVideo = videoTrack.setEnabled(enabled);
            if(enabledVideo && enabled) {
                ((CameraCapturerImpl)cameraCapturer).resume();
            } else if(enabledVideo && !enabled){
                ((CameraCapturerImpl)cameraCapturer).pause();
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
        ((CameraCapturerImpl)cameraCapturer).resetNativeVideoCapturer();
        cameraCapturer = null;
    }

    @Override
    public VideoConstraints getVideoConstraints() {
        return videoConstraints;
    }

}
