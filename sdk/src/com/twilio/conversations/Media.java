package com.twilio.conversations;

import java.util.List;

public interface Media {

	/**
	 * Retrieves list of video tracks
	 * 
	 * @return list of video tracks
	 */
	public List<VideoTrack> getVideoTracks();

	/**
	 * Retrieves list of audio tracks
	 *
	 * @return list of audio tracks
	 */
	public List<AudioTrack> getAudioTracks();

}
