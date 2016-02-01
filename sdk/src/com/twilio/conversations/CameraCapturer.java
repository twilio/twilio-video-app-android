package com.twilio.conversations;

/**
 * A camera capturer retrieves frames from a
 * camera source on the device which can be previewed onto
 * a view.
 *
 */
public interface CameraCapturer {

	/**
     * Camera source types
	 *
	 */
	enum CameraSource {
		CAMERA_SOURCE_FRONT_CAMERA,
		CAMERA_SOURCE_BACK_CAMERA
	};

	/**
	 * Starts previewing the camera.
	 *
	 */
	void startPreview();

	/**
	 * Stops previewing the camera.
	 *
	 */
	void stopPreview();

	/**
	 * Returns whether the camera capturer is previewing the camera
	 *
	 */
	boolean isPreviewing();

	/**
	 * Switches the camera to the next available camera source.
	 *
	 */
	boolean switchCamera();

	/**
	 * Pauses the capturer when previewing or sharing the camera during a conversation
	 *
	 */
	void pause();

	/**
	 * Resumes the capturer to preview or share the camera during a conversation
	 *
	 */
	void resume();

}