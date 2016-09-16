package com.twilio.video;

/**
 * A local video track that gets video frames from a specified {@link VideoCapturer}
 */
public class LocalVideoTrack extends VideoTrack {
    private static final Logger logger = Logger.getLogger(LocalAudioTrack.class);

    private long nativeLocalVideoTrackHandle;
    private final VideoCapturer videoCapturer;
    private final VideoConstraints videoConstraints;

    LocalVideoTrack(long nativeLocalVideoTrackHandle,
                    VideoCapturer videoCapturer,
                    VideoConstraints videoConstraints,
                    org.webrtc.VideoTrack webrtcVideoTrack) {
        super(webrtcVideoTrack);
        this.nativeLocalVideoTrackHandle = nativeLocalVideoTrackHandle;
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

    /**
     * Retrieves the video constraints associated with this track
     *
     * If you do not provide any video constraints, the default video constraints are set to a
     * maximum video dimension size of 640x480 at a frame rate of 30 frames per second.
     *
     * @return video constraints
     */
    public VideoConstraints getVideoConstraints() {
        return videoConstraints;
    }

    /**
     * Check if local video track is enabled
     * @return true if the local video is enabled
     */
    @Override
    public boolean isEnabled() {
        if (!isReleased()) {
            return nativeIsEnabled(nativeLocalVideoTrackHandle);
        } else {
            logger.e("Local video track is not enabled because it has been removed");
            return false;
        }
    }

    /**
     * Sets the state of the local video track
     *
     * @param enabled the desired state of the local video track
     */
    public void enable(boolean enabled) {
        if (!isReleased()) {
            nativeEnable(nativeLocalVideoTrackHandle, enabled);
        } else {
            logger.e("Cannot enable a local video track that has been removed");
        }
    }

    synchronized void release() {
        if (!isReleased()) {
            super.release();
            nativeRelease(nativeLocalVideoTrackHandle);
            nativeLocalVideoTrackHandle = 0;
        }
    }

    boolean isReleased() {
        return nativeLocalVideoTrackHandle == 0;
    }

    private native boolean nativeIsEnabled(long nativeLocalVideoTrackHandle);
    private native void nativeEnable(long nativeLocalVideoTrackHandle, boolean enable);
    private native void nativeRelease(long nativeLocalVideoTrackHandle);
}