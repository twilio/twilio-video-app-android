package com.twilio.signal;

import android.view.ViewGroup;

public interface CameraCapturer extends VideoCapturer{
	
	/** Camera Capture Source */
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

	/**
	 * Flips the camera that is being shared.
	 *
	 */
	public boolean switchCamera(Runnable switchDoneEvent);

	public  ViewGroup getCapturerView();

}