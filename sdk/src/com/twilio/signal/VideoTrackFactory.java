package com.twilio.signal;

import com.twilio.signal.impl.VideoTrackImpl;

public class VideoTrackFactory {
	
	public static LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer) {
		return new VideoTrackImpl(cameraCapturer);
	}

}
