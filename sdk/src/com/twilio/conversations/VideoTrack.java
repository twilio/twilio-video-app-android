package com.twilio.conversations;

import java.util.List;

public interface VideoTrack extends MediaTrack {

	public void addRenderer(VideoRenderer videoRenderer);

	public void removeRenderer(VideoRenderer videoRenderer);

	public List<VideoRenderer> getRenderers();

}

