package com.twilio.conversations;

import com.twilio.conversations.impl.LocalVideoTrackImpl;

/**
 * A factory for creating an instance of {@link LocalVideoTrack}
 *
 */
public class LocalVideoTrackFactory {
    /**
     * Create new instance of local video track
     *
     * @param cameraCapturer
     * @return new instance of LocalVideoTrack
     */
    public static LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer) {
        return new LocalVideoTrackImpl(cameraCapturer);
    }

    /**
     * Create new instance of local video track with {@link VideoConstraints}
     *
     * If you do not provide any video constraints, the default video constraints are set to a
     * a minimum of 10 frames per second, a maximum of 30 frames per second, and a maximum video
     * dimension size of 640x480.
     *
     * @param cameraCapturer
     * @param videoConstraints
     * @return new instance of LocalVideoTrack
     */
    public static LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer, VideoConstraints videoConstraints) {
        return new LocalVideoTrackImpl(cameraCapturer, videoConstraints);
    }

}

