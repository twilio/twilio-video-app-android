package com.twilio.signal;

import android.view.ViewGroup;

import com.twilio.signal.impl.MediaImpl;

public class LocalMedia extends MediaImpl {
	
	private ViewGroup container;
	private CameraCapturer camera;
	
	
	/** Read-only representation of the local video container. */
	public ViewGroup getContainerView() {
		return container;
	}

	/** Local video view container */
	public void attachContainerView(ViewGroup container) {
		this.container = container;
	}

	/**
	 * Specifies whether or not your local audio should be muted
	 *
	 * @param on <code>true</code> if local audio should be muted, false otherwise
	 */
	public void mute(boolean on) {
		
	}

	/**
	 * Indicates whether your local audio is muted.
	 *
	 * @return <code>true</code> if local audio is muted, false otherwise
	 */
	public boolean isMuted() {
		return false;
	}
}
