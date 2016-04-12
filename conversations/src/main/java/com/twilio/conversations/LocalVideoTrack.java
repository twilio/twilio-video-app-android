package com.twilio.conversations;

/**
 * A local video track that gets camera video from a {@link CameraCapturer}
 *
 */
public interface LocalVideoTrack extends VideoTrack {
    /**
     * Retrieves the {@link CameraCapturer} associated with this video track
     *
     * @return camera
     */
    CameraCapturer getCameraCapturer();

    /**
     * Specifies whether or not your camera video should be shared
     *
     * @param enabled <code>true</code> if camera should be shared, false otherwise
     * @return true if the operation succeeded. false if there is an operation in progress.
     */
    boolean enable(boolean enabled);

    /**
     * Specifies the video constraints associated with this track
     *
     * If you do not provide any video constraints, the default video constraints are set to a
     * a minimum of 10 frames per second, a maximum of 30 frames per second, and a maximum video
     * dimension size of 640x480.
     *
     * @return video constraints
     */
    VideoConstraints getVideoConstraints();

}