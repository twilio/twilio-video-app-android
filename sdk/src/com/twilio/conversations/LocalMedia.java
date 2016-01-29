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
	 * in TOO_MANY_TRACKS, or INVALID_VIDEO_CAPTURER error condition.
	 * @return true if the local video track was successfully added or false
	 * if the local video track could not be added because a track operation is in
	 * progress.
	 */
	boolean addLocalVideoTrack(LocalVideoTrack track) throws TwilioConversationsException;

	/**
	 * Removes the local video track from list of tracks.
	 * 
	 * @param track
	 * @throws TwilioConversationsException Removing a local video track that has
	 * already been removed will result in a INVALID_VIDEO_TRACK_STATE error condition.
	 * @return true if the local video track was successfully removed or false
	 * if the local video track could not be removed because a track operation is in
	 * progress.
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