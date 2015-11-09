package com.twilio.signal;

import com.twilio.signal.impl.VideoTrackImpl;

public class LocalVideoTrack extends VideoTrackImpl {
	
	private CameraCapturer cameraCapturer;
	
	public LocalVideoTrack(CameraCapturer cameraCapturer) {
		super();
		this.cameraCapturer = cameraCapturer;
	}
	
	/**
	 * Gets the camera that is being shared.
	 *
	 * @return camera
	 */
	public CameraCapturer getCameraCapturer() {
		return cameraCapturer;
	}

}
