package com.twilio.signal;

import android.graphics.SurfaceTexture;

public class LocalMediaImpl implements LocalMedia {
	private SurfaceTexture view;

	public LocalMediaImpl() {}

	@Override
	public SurfaceTexture getView() {
		return view;
	}

	@Override
	public void attachView(SurfaceTexture view) {
		this.view = view;
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

}
