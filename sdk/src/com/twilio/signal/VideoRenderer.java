package com.twilio.signal;

public interface VideoRenderer {

	public void setSize(int width, int height);

	public void renderFrame(I420Frame frame);

	public void setObserver(VideoRendererObserver observer);

}

