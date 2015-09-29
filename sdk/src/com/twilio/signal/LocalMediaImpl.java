package com.twilio.signal;

import android.view.ViewGroup;
import java.util.List;

import com.twilio.signal.impl.MediaImpl;

public class LocalMediaImpl extends MediaImpl implements LocalMedia {
	private ViewGroup container;

	public LocalMediaImpl() {}

	@Override
	public ViewGroup getContainerView() {
		return container;
	}

	@Override
	public void attachContainerView(ViewGroup container) {
		this.container = container;
	}

	@Override
	public void setCamera(VideoCaptureSource camera) {
		// TODO Auto-generated method stub

	}

	@Override
	public VideoCaptureSource getCamera() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableCamera(boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCameraEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void mute(boolean on) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMuted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void pauseVideo(boolean paused) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startCameraPreview() {
		// TODO Auto-generated method stub

	}

	@Override
	public void endCameraPreview() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flipCamera() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<VideoTrack> getVideoTracks() {
		return null;
	}

}
