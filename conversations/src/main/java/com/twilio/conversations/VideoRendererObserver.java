package com.twilio.conversations;

/**
 * A video renderer observer provides information
 * about frames being sent from a {@link VideoTrack}
 */
public interface VideoRendererObserver {
	/**
	 * This method notifies the observer when the first frame has arrived.
	 */
	void onFirstFrame();

	/**
	 * This method notifies the observer when the frame dimensions have changed.
	 *
	 * @param width frame width
	 * @param height frame height
     * @param rotation frame rotation
	 */
	void onFrameDimensionsChanged(int width, int height, int rotation);
}
