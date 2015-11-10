package com.twilio.signal;

import java.util.List;

public interface Media {

	/**
	 * Retrieves video tracks
	 * 
	 * @return
	 */
	public List<VideoTrack> getVideoTracks();

}
