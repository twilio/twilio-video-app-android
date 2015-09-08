package com.twilio.signal.impl;

public class VideoSurface {

	public static interface Observer {
		public void onDidAddVideoTrack();
		public void onDidRemoveVideoTrack();
		public void onDidReceiveVideoTrackEvent();
	}

	private final long nativeVideoSurface;
	private final long nativeObserver;

	VideoSurface(long nativeVideoSurface, long nativeObserver) {
    		this.nativeVideoSurface = nativeVideoSurface;
    		this.nativeObserver = nativeObserver;
  	}

	public void dispose() {
		freeVideoSurface(nativeVideoSurface);
		freeObserver(nativeObserver);
	}

	private static native void freeVideoSurface(long nativeVideoSurface);

	private static native void freeObserver(long nativeObserver);

}
