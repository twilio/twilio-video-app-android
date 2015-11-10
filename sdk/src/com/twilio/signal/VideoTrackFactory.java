package com.twilio.signal;

import com.twilio.signal.impl.VideoTrackImpl;

public class VideoTrackFactory {
	
	/**
	 * Create new instance of local video track
	 * @param cameraCapturer
	 * @return new instance of VideoTrack
	 */
	public static LocalVideoTrack createLocalVideoTrack(CameraCapturer cameraCapturer) {
		return new VideoTrackImpl(cameraCapturer);
	}

}
