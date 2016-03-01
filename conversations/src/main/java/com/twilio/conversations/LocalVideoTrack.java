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

}