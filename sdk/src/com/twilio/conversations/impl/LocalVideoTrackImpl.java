package com.twilio.conversations.impl;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.LocalVideoTrack;

public class LocalVideoTrackImpl extends VideoTrackImpl implements  LocalVideoTrack  {
	
	private CameraCapturer cameraCapturer;
	private boolean enableVideo;
	
	public LocalVideoTrackImpl(CameraCapturer cameraCapturer) {
		super();
		this.cameraCapturer = cameraCapturer;
		enableVideo = true;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.LocalVideoTrack#getCameraCapturer()
	 */
	@Override
	public CameraCapturer getCameraCapturer() {
		return cameraCapturer;
	}
	
	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#enableCamera(boolean)
	 */
	@Override
	public void enableCamera(boolean enabled) {
		org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
		if (videoTrack != null) {
			videoTrack.setEnabled(enabled);
		}
		enableVideo = enabled;
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#isCameraEnabled()
	 */
	@Override
	public boolean isCameraEnabled() {
		org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
		if (videoTrack != null) {
			enableVideo = videoTrack.enabled();
		}
		return enableVideo;
	}
	
	void removeCameraCapturer() {
		((CameraCapturerImpl)cameraCapturer).resetNativeVideoCapturer();
		cameraCapturer = null;
	}
	
}
