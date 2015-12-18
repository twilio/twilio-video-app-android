package com.twilio.conversations;

public interface LocalVideoTrack extends VideoTrack {

	/**
	 * Retrieves the camera that is being shared.
	 *
	 * @return camera
	 */
	public CameraCapturer getCameraCapturer();

	/**
	 * Specifies whether or not your camera video is being shared
	 *
	 * @param enabled <code>true</code> if camera should be shared, false otherwise
	 */
	public void enableCamera(boolean enabled);

	/**
	 * Gets whether or not your camera video is being shared
	 *
	 * @return <code>true</code> if camera is being shared, false otherwise
	 */
	public boolean isCameraEnabled();

}