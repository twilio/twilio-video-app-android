package com.twilio.signal;


public interface CameraCapturer {
	
	public static enum CameraSource {
		CAMERA_SOURCE_FRONT_CAMERA, ///< Front facing device camera
		CAMERA_SOURCE_BACK_CAMERA ///< Back facing device camera
	};

	/**
	 * Start previewing the local camera.
	 *
	 */
	public boolean startPreview();

	/**
	 * Stop previewing the local camera.
	 *
	 */
	public boolean stopPreview();

	/*
	 * Returns whether the camera capturer is previewing the camera
	 */
	public boolean isPreviewing();

	/**
	 * Switches the camera to the next available device.
	 *
	 */
	public boolean switchCamera(Runnable switchDoneEvent);

}