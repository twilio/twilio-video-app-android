package com.twilio.signal.impl;

import java.util.ArrayList;
import java.util.List;

import android.view.ViewGroup;

import com.twilio.signal.LocalMedia;
import com.twilio.signal.LocalVideoTrack;

public class LocalMediaImpl extends MediaImpl implements LocalMedia {
	
	private ViewGroup container;
	
	
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
	public void addLocalVideoTrack(LocalVideoTrack track) {
		if (track instanceof VideoTrackImpl) {
			addVideoTrack((VideoTrackImpl)track);
		} else {
			// TODO : probably throw exception,
			// we don't support custom video tracks for now
		}
		
	}
	
	
}
