package com.twilio.conversations;

import java.util.List;


public interface LocalMedia {

	/**
	 * Returns the local video tracks
	 * 
	 * @return list of local video tracks
	 */
	public List<LocalVideoTrack> getLocalVideoTracks();

	/**
	 * Adds a local video track to list of tracks.
	 * 
	 * @param track
	 */
	public void addLocalVideoTrack(LocalVideoTrack track);

	/**
	 * Removes the local video track from list of tracks.
	 * 
	 * @param track
	 */
	public boolean removeLocalVideoTrack(LocalVideoTrack track);

	/**
	 * Specifies whether or not your local audio should be muted
	 *
	 * @param on <code>true</code> if local audio should be muted, false otherwise
	 * @return <code>true</code> if mute operation is successful
	 */
	public boolean mute(boolean on);

	/**
	 * Indicates whether your local audio is muted.
	 *
	 * @return <code>true</code> if local audio is muted, false otherwise
	 */
	public boolean isMuted();

	/**
	 * Enables local audio to media session.
	 *
	 * @return true if local audio is enabled
	 */
	public boolean addMicrophone();

	/**
	 * Disables local audio from the media session.
	 * 
	 * @return true if local audio is disabled
	 */
	public boolean removeMicrophone();

	/**
	 * Indicates whether or not your local
	 * audio is enabled in the media session
	 * 
	 * @return true if local audio is enabled
	 */
	public boolean isMicrophoneAdded();

}