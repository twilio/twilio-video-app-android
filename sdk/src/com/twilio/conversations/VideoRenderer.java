package com.twilio.conversations;

/**
 * A VideoRenderer is used to receive frames from a {@link VideoTrack}
 *
 */
public interface VideoRenderer {
	/**
	 * Provides the YUV frame in I420 format
	 *
	 * @param frame I420 YUV frame
	 */
	void renderFrame(I420Frame frame);
}

