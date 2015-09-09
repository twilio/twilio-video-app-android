package com.twilio.signal.impl;

import org.webrtc.VideoRenderer;
import android.opengl.GLSurfaceView;
import java.util.Map;
import java.util.HashMap;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import com.twilio.signal.Participant;

public class VideoSurface {

	public static interface Observer {
		public void onDidAddVideoTrack();
		public void onDidRemoveVideoTrack();
		public void onDidReceiveVideoTrackEvent(VideoRenderer.I420Frame frame);
	}

	private final long nativeVideoSurface;
	private final long nativeObserver;

	private GLSurfaceView localView;
	private volatile VideoRenderer.Callbacks localRendererCallbacks;

	private Map<Participant, GLSurfaceView> views;

	VideoSurface(long nativeVideoSurface, long nativeObserver) {
    		this.nativeVideoSurface = nativeVideoSurface;
    		this.nativeObserver = nativeObserver;
		this.views = new HashMap<Participant, GLSurfaceView>();
  	}

	public void attachLocalView(GLSurfaceView localView) {
		this.localView = localView;
		try {
			VideoRendererGui.setView(localView, new Runnable() {
					@Override
					public void run() {
					}
					});
		} catch(Throwable t) {

		}
	}

	public VideoRenderer.Callbacks getLocalVideoRendererCallbacks() {
		return localRendererCallbacks;
	}

	public VideoRenderer.Callbacks createLocalVideoRenderer() {
		localRendererCallbacks = VideoRendererGui.createGuiRenderer(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
		return localRendererCallbacks;
	}

	public void attachView(Participant participant, GLSurfaceView view) {
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
