package com.twilio.signal;

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

	/**
	 * Specifies whether or not your local video should be paused
	 *
	 * @param paused <code>true</code> if local video should be paused, false otherwise
	 */
	public void pauseVideo(boolean paused);

	/**
	 * Indicates whether your local video is paused.
	 *
	 * @return <code>true</code> if local video is paused, false otherwise
	 */
	public boolean isPaused();

}