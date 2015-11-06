package com.twilio.signal;

import com.twilio.signal.impl.VideoTrackImpl;

public class LocalVideoTrack extends VideoTrackImpl {
	
	private CameraCapturer cameraCapturer;
	
	public LocalVideoTrack(CameraCapturer cameraCapturer) {
		super();
		this.cameraCapturer = cameraCapturer;
	}
	
	public CameraCapturer getCameraCapturer() {
		return cameraCapturer;
	}

}
