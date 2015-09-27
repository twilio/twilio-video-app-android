package com.twilio.signal;

import java.util.List;

public interface VideoTrack {

	public void addRenderer(VideoRenderer videoRenderer);

	public void removeRenderer(VideoRenderer videoRenderer);

	public List<VideoRenderer> getRenderers();

}

