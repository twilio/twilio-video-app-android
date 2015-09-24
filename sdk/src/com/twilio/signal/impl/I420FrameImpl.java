package com.twilio.signal.impl;

import com.twilio.signal.I420Frame;
import java.nio.ByteBuffer;

public class I420FrameImpl implements I420Frame {
	org.webrtc.VideoRenderer.I420Frame frame;

	public I420FrameImpl(org.webrtc.VideoRenderer.I420Frame frame) {
		this.frame = frame;
	} 

	public org.webrtc.VideoRenderer.I420Frame getRawFrame() {
		return frame;
	}
}
