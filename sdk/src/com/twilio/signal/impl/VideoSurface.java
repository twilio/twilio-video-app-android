package com.twilio.signal.impl;

import org.webrtc.VideoRenderer;
import android.view.Surface;
import java.util.Map;
import java.util.HashMap;

import com.twilio.signal.Participant;

public class VideoSurface {

	public static interface Observer {
		public void onDidAddVideoTrack();
		public void onDidRemoveVideoTrack();
		public void onDidReceiveVideoTrackEvent();
	}

	private final long nativeVideoSurface;
	private final long nativeObserver;

	private Surface localView;
	private Map<Participant, Surface> views;

	VideoSurface(long nativeVideoSurface, long nativeObserver) {
    		this.nativeVideoSurface = nativeVideoSurface;
    		this.nativeObserver = nativeObserver;
		this.views = new HashMap<Participant, Surface>();
  	}

	public void attachLocalView(Surface localView) {
		this.localView = localView;
	}

	public void attachView(Participant participant, Surface view) {
		views.put(participant, view);
	}

	public void dispose() {
		freeVideoSurface(nativeVideoSurface);
		freeObserver(nativeObserver);
	}

	public long getNativeHandle() {
		return nativeVideoSurface;
	}

	private static native void freeVideoSurface(long nativeVideoSurface);

	private static native void freeObserver(long nativeObserver);

}
