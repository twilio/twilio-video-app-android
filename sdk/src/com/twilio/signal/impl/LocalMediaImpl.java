package com.twilio.signal.impl;

import java.util.ArrayList;
import java.util.List;

import android.view.ViewGroup;

import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;
import com.twilio.signal.VideoTrack;

public class LocalMediaImpl implements LocalMedia {
	
	private ViewGroup container;
	private List<LocalVideoTrackImpl> videoTracksImpl = new ArrayList<LocalVideoTrackImpl>();
	
	private static int MAX_LOCAL_VIDEO_TRACKS = 1;
	
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#getContainerView()
	 */
	@Override
	public ViewGroup getContainerView() {
		return container;
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#attachContainerView(android.view.ViewGroup)
	 */
	@Override
	public void attachContainerView(ViewGroup container) {
		this.container = container;
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#mute(boolean)
	 */
	@Override
	public void mute(boolean on) {
		
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalMedia#isMuted()
	 */
	@Override
	public boolean isMuted() {
		return false;
	}

	@Override
	public List<LocalVideoTrack> getLocalVideoTracks() {
		return new ArrayList<LocalVideoTrack>(videoTracksImpl);
	}

	@Override
	public void addLocalVideoTrack(LocalVideoTrack track)
			throws IllegalArgumentException, UnsupportedOperationException {
		if (track instanceof LocalVideoTrackImpl) {
			if (videoTracksImpl.size() < MAX_LOCAL_VIDEO_TRACKS) {
				videoTracksImpl.add((LocalVideoTrackImpl)track);
			} else {
				throw new UnsupportedOperationException("Maximum size " + MAX_LOCAL_VIDEO_TRACKS + " of LocalVideoTracks reached.");
			}
		} else {
			throw new IllegalArgumentException("Only TwilioSDK LocalVideoTrack implementation is supported");
		}
		
	}
	
	public void removeVideoTrack(LocalVideoTrackImpl videoTrackImpl) {
		videoTracksImpl.remove(videoTrackImpl);
	}

	/*
	 * Media interface
	 */
	@Override
	public List<VideoTrack> getVideoTracks() {
		return new ArrayList<VideoTrack>(videoTracksImpl);
	}
	
	
}
