package com.twilio.conversations;

/**
 * A VideoRenderer is used to receive frames from a {@link VideoTrack}
 *
 */
public interface VideoRenderer {

	/**
	 * Sets the size associated with the frame
	 * @param width width of frame
	 * @param height height of frame
	 */
	public void setSize(int width, int height);

	/**
	 * Provides the YUV frame in I420 format
	 *
	 * @param frame I420 YUV frame
	 */
	public void renderFrame(I420Frame frame);

	/**
	 * Set an observer to get notified of frame events
	 * @param observer
	 */
	public void setObserver(VideoRendererObserver observer);

}

