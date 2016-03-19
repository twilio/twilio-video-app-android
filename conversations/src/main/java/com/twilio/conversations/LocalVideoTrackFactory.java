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
     * @return new instance of VideoTrack
     */
    public static LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer) {
        return new LocalVideoTrackImpl(cameraCapturer);
    }

    /**
     * Create new instance of local video track
     *
     * @param cameraCapturer
     * @param videoConstraints
     * @return new instance of VideoTrack
     */
    public static LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer, VideoConstraints videoConstraints) {
        // TODO: implement me
        return null;
    }

}

