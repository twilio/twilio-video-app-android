package com.twilio.conversations;

import java.util.List;

/**
 * Provides video and audio tracks associated with a {@link Participant}
 *
 */
public interface Media {

	/**
	 * Retrieves the list of video tracks
	 * 
	 * @return list of video tracks
	 */
	public List<VideoTrack> getVideoTracks();

	/**
	 * Retrieves the list of audio tracks
	 *
	 * @return list of audio tracks
	 */
	public List<AudioTrack> getAudioTracks();

}
