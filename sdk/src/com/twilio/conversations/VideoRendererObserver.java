package com.twilio.conversations;

/**
 * A video renderer observer provides information
 * about frames being sent from a {@link VideoTrack}
 */
public interface VideoRendererObserver {

	/**
	 * This method notifies the observer when the first frame has arrived.
	 */
	public void onFirstFrame();

	/**
	 * This method notifies the observer when the frame dimensions have changed.
	 *
	 * @param width frame width
	 * @param height frame height
	 */
	public void onFrameDimensionsChanged(int width, int height);

}

