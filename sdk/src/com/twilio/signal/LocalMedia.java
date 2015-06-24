package com.twilio.signal;

public interface LocalMedia extends Media {

	/** Video Capture Source */
	public static enum VideoCaptureSource {
		VIDEO_CAPTYRE_SOURCE_FRONT_CAMERA, ///< Front facing device camera
		VIDEO_CAPTURE_SOURCE_BACK_CAMERA ///< Back facing device camera
	};

	/**
	 * Sets the camera that is being shared.
	 *
	 * @param camera
	 */
	public void setCamera(VideoCaptureSource camera);

	/**
	 * Gets the camera that is being shared.
	 *
	 * @return camera
	 */
	public VideoCaptureSource getCamera();

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
	 * Specifies whether or not your local audio should be muted
	 *
	 * @param on <code>true</code> if local audio should be muted, false otherwise
	 */
	public void mute(boolean on);

	/**
	 * Indicates whether your local audio is muted.
	 *
	 * @return <code>true</code> if local audio is muted, false otherwise
	 */
	public boolean isMuted();

	/**
	 * Specifies whether or not your local vido should be paused
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
	 * Start previewing the local camera.
	 *
	 */
	public void startCameraPreview();

	/**
	 * Stop previewing the local camera.
	 *
	 */
	public void endCameraPreview();

	/**
	 * Flips the camera that is being shared.
	 *
	 */
	public void flipCamera();

}
