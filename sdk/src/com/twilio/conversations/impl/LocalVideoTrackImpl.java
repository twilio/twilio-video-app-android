package com.twilio.conversations.impl;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.LocalVideoTrack;
import com.twilio.conversations.MediaTrackState;

public class LocalVideoTrackImpl extends VideoTrackImpl implements LocalVideoTrack  {

	private CameraCapturer cameraCapturer;
	private boolean enabledVideo = true;

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
			enabledVideo = videoTrack.setEnabled(enabled);
		} else {
			enabledVideo = enabled;
		}
		return enabledVideo;
	}

	@Override
	public boolean isCameraEnabled() {
		org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
		if (videoTrack != null) {
			return videoTrack.enabled();
		} else {
			return enabledVideo;
		}
	}

	void removeCameraCapturer() {
		((CameraCapturerImpl)cameraCapturer).resetNativeVideoCapturer();
		cameraCapturer = null;
	}

}
