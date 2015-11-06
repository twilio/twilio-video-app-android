package com.twilio.signal;

import android.view.ViewGroup;

import com.twilio.signal.impl.MediaImpl;

public class LocalMedia extends MediaImpl {
	
	private ViewGroup container;
	private CameraCapturer camera;
	
	
	/** Read-only representation of the local video container. */
	public ViewGroup getContainerView() {
		return container;
	}

	/** Local video view container */
	public void attachContainerView(ViewGroup container) {
		this.container = container;
	}

	/**
	 * Sets the camera that is being shared.
	 *
	 * @param camera
	 */
	public void setCamera(CameraCapturer camera) {
		this.camera = camera;
	}

	/**
	 * Gets the camera that is being shared.
	 *
	 * @return camera
	 */
	public CameraCapturer getCamera() {
		return camera;
	}

	/**
	 * Specifies whether or not your camera video is being shared
	 *
	 * @param enabled <code>true</code> if camera should be shared, false otherwise
	 */
	public void enableCamera(boolean enabled) {
		
	}

	/**
	 * Gets whether or not your camera video is being shared
	 *
	 * @return <code>true</code> if camera is being shared, false otherwise
	 */
	public boolean isCameraEnabled() {
		return false;
	}

	/**
	 * Specifies whether or not your local audio should be muted
	 *
	 * @param on <code>true</code> if local audio should be muted, false otherwise
	 */
	public void mute(boolean on) {
		
	}

	/**
	 * Indicates whether your local audio is muted.
	 *
	 * @return <code>true</code> if local audio is muted, false otherwise
	 */
	public boolean isMuted() {
		return false;
	}

	/**
	 * Specifies whether or not your local vido should be paused
	 *
	 * @param paused <code>true</code> if local video should be paused, false otherwise
	 */
	public void pauseVideo(boolean paused) {
		
	}

	/**
	 * Indicates whether your local video is paused.
	 *
	 * @return <code>true</code> if local video is paused, false otherwise
	 */
	public boolean isPaused() {
		return false;
	}

	/**
	 * Start previewing the local camera.
	 *
	 */
	public void startCameraPreview() {
		
	}

	/**
	 * Stop previewing the local camera.
	 *
	 */
	public void endCameraPreview() {
		
	}

	/**
	 * Flips the camera that is being shared.
	 *
	 */
	public void flipCamera() {
		
	}

}
