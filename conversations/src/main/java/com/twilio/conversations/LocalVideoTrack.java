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
	public CameraCapturer getCameraCapturer();

	/**
	 * Specifies whether or not your camera video should be shared
	 *
	 * @param enabled <code>true</code> if camera should be shared, false otherwise
	 * @return true if the operation succeeded.
	 */
	public boolean enable(boolean enabled);

	/**
	 * Returns whether or not your camera video is being shared
	 *
	 * @return <code>true</code> if camera is being shared, false otherwise
	 */
	public boolean isEnabled();

}