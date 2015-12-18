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
	public static enum CameraSource {
		CAMERA_SOURCE_FRONT_CAMERA,
		CAMERA_SOURCE_BACK_CAMERA
	};

	/**
	 * Starts previewing the camera.
	 *
	 */
	public void startPreview();

	/**
	 * Stops previewing the camera.
	 *
	 */
	public void stopPreview();

	/**
	 * Returns whether the camera capturer is previewing the camera
	 *
	 */
	public boolean isPreviewing();

	/**
	 * Switches the camera to the next available camera source.
	 *
	 */
	public boolean switchCamera();

}