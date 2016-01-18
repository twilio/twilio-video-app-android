package com.twilio.conversations;

import com.twilio.conversations.impl.I420Frame;

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
	public void renderFrame(I420Frame frame);
}

