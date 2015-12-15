package com.twilio.signal;

import java.util.List;

import android.view.ViewGroup;

public interface LocalMedia extends Media {
	
	/**
	 * Retrieves local video tracks
	 * 
	 * @return list of local video tracks
	 */
	public List<LocalVideoTrack> getLocalVideoTracks();
	
	/**
	 * Add local video track to list of tracks.
	 * 
	 * @param track
	 */
	public void addLocalVideoTrack(LocalVideoTrack track);
	
	/**
	 * Remove local video track from list of tracks.
	 * 
	 * @param track
	 */
	public boolean removeLocalVideoTrack(LocalVideoTrack track);

	/** Get local video view container. */
	public ViewGroup getContainerView();

	/** Attach local video view container */
	public void attachContainerView(ViewGroup container);

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
	 * Add local audio track to media session
	 * @return true if local audio track is added
	 */
	public boolean addMicrophone();
	
	/**
	 * Remove local audio track from media session
	 * 
	 * @return true if local audio track is removed
	 */
	public boolean removeMicrophone();
	
	/**
	 * Indicates whether or not your local
	 * audio track is added to media sessio
	 * 
	 * @return true if local audio track is added
	 */
	public boolean isMicrophoneAdded();

}