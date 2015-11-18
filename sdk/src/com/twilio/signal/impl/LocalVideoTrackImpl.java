package com.twilio.signal.impl;

import com.twilio.signal.CameraCapturer;
import com.twilio.signal.LocalVideoTrack;

public class LocalVideoTrackImpl extends VideoTrackImpl implements  LocalVideoTrack  {
	
	private CameraCapturer cameraCapturer;
	
	public LocalVideoTrackImpl(CameraCapturer cameraCapturer) {
		super();
		this.cameraCapturer = cameraCapturer;
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
	}

	/* (non-Javadoc)
	 * @see com.twilio.signal.CameraCapturer#isCameraEnabled()
	 */
	@Override
	public boolean isCameraEnabled() {
		org.webrtc.VideoTrack videoTrack = getWebrtcVideoTrack();
		if (videoTrack != null) {
			return videoTrack.enabled();
		}
		return false;
	}
	
	void removeCameraCapturer() {
		cameraCapturer = null;
	}
	
}
