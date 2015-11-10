package com.twilio.signal;

public interface LocalVideoTrack extends VideoTrack{

	/**
	 * Retrieves the camera that is being shared.
	 *
	 * @return camera
	 */
	public abstract CameraCapturer getCameraCapturer();

}