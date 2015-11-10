package com.twilio.signal;

import java.util.List;

import android.view.ViewGroup;

public interface LocalMedia extends Media{
	
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

	/** Read-only representation of the local video container. */
	public abstract ViewGroup getContainerView();

	/** Local video view container */
	public abstract void attachContainerView(ViewGroup container);

	/**
	 * Specifies whether or not your local audio should be muted
	 *
	 * @param on <code>true</code> if local audio should be muted, false otherwise
	 */
	public abstract void mute(boolean on);

	/**
	 * Indicates whether your local audio is muted.
	 *
	 * @return <code>true</code> if local audio is muted, false otherwise
	 */
	public abstract boolean isMuted();

}