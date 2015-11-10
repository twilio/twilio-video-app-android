package com.twilio.signal;

import java.util.List;

public interface Media {

	/**
	 * Retrieves list of video tracks
	 * 
	 * @return
	 */
	public List<VideoTrack> getVideoTracks();

}
