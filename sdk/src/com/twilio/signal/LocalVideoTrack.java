package com.twilio.signal;

public interface LocalVideoTrack extends VideoTrack{

	/**
	 * Gets the camera that is being shared.
	 *
	 * @return camera
	 */
	public abstract CameraCapturer getCameraCapturer();

}