package com.twilio.conversations.impl;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.LocalVideoTrack;

public class LocalVideoTrackImpl extends VideoTrackImpl implements  LocalVideoTrack  {

	private CameraCapturer cameraCapturer;

	public LocalVideoTrackImpl(CameraCapturer cameraCapturer) {
		super();
		this.cameraCapturer = cameraCapturer;
	}

	@Override
	public CameraCapturer getCameraCapturer() {
		return cameraCapturer;
	}

	@Override
	public boolean enableCamera(boolean enabled) {
		org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
		if (videoTrack != null) {
			return videoTrack.setEnabled(enabled);
		}
		return cameraCapturer != null;
	}

	@Override
	public boolean isCameraEnabled() {
		org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
		if (videoTrack != null) {
			return videoTrack.enabled();
		}
		return cameraCapturer != null;
	}

	void removeCameraCapturer() {
		((CameraCapturerImpl)cameraCapturer).resetNativeVideoCapturer();
		cameraCapturer = null;
	}

}
