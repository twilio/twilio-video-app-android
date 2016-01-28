package com.twilio.conversations;

import java.util.List;


public interface LocalMedia {

	/**
	 * Returns the local video tracks
	 * 
	 * @return list of local video tracks
	 */
	List<LocalVideoTrack> getLocalVideoTracks();

	/**
	 * Adds a local video track to list of tracks.
	 * 
	 * @param track
	 * @throws TwilioConversationsException Adding a local video track can result
	 * in TRACK_OPERATION_IN_PROGRESS, TOO_MANY_TRACKS, or INVALID_VIDEO_CAPTURER
	 * error condition.
	 * @return the local video track was successfully added
	 */
	boolean addLocalVideoTrack(LocalVideoTrack track) throws TwilioConversationsException;

	/**
	 * Removes the local video track from list of tracks.
	 * 
	 * @param track
	 * @throws TwilioConversationsException
	 * @return the local video track was successfully removed
	 */
	boolean removeLocalVideoTrack(LocalVideoTrack track) throws TwilioConversationsException;

	/**
	 * Specifies whether or not your local audio should be muted
	 *
	 * @param on <code>true</code> if local audio should be muted, false otherwise
	 * @return <code>true</code> if mute operation is successful
	 */
	boolean mute(boolean on);

	/**
	 * Indicates whether your local audio is muted.
	 *
	 * @return <code>true</code> if local audio is muted, false otherwise
	 */
	boolean isMuted();

	/**
	 * Enables local audio to media session.
	 *
	 * @return true if local audio is enabled
	 */
	boolean addMicrophone();

	/**
	 * Disables local audio from the media session.
	 * 
	 * @return true if local audio is disabled
	 */
	boolean removeMicrophone();

	/**
	 * Indicates whether or not your local
	 * audio is enabled in the media session
	 * 
	 * @return true if local audio is enabled
	 */
	boolean isMicrophoneAdded();

}