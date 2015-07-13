package com.twilio.signal.impl;


/**
 * This interface describes the events that the Stream’s listener should handle.
 *
 */

public interface StreamListener {
	
	/**
	 * This method notifies the listener that a stream was obtained with the specified constraints.
	 * 
	 * @param stream The stream that was obtained.
	 */
	public void onGetStreamWithConstraints(Stream stream);

	/**
	 * This method notifies the listener that it failed to obtain a stream with the specified constraints.
	 * 
	 * @param error The error that caused the failure.
	 */
	public void onFailToGetStreamWithError(String error);

	/**
	 * This method notifies the listener that a stream containing a video track from the front facing camera was obtained.
	 * 
	 * @param stream The stream that was obtained.
	 */
	public void onGetVideoFromFrontFacingCamera(Stream stream);

	/**
	 * This method notifies the listener that it failed to obtain a stream containing a video track from the front facing camera.
	 * 
	 * @param error The error that caused the failure.
	 */
	public void onFailToGetVideoFromFrontFacingCameraWithError(String error);

	/**
	 * This method notifies the listen that a stream containing a video track from the rear facing camera was obtained
	 * 
	 * @param stream The stream that was obtained.
	 */
	public void onGetVideoFromRearFacingCamera(Stream stream);
	
	/**
	 * This method notifies the listener that it failed to obtain a stream containing a video track from the rear facing camera.
	 * 
	 * @param error The error that caused the failure.
	 */

	public void onFailToGetVideoFromRearFacingCameraWithError(String error);

	/**
	 * 
	 * This method notifies the listener that a stream containing a video screen capture was obtained.
	 * 
	 * @param stream The stream that was obtained.
	 */
	public void onAddScreenCapture(Stream stream);
	
	/**
	 * This method notifies the listener that it failed to obtain a video screen capture.
	 * 
	 * @param error The error that caused the failure.
	 */

	public void onFailToAddScreenCaptureWithError(String error);

}
