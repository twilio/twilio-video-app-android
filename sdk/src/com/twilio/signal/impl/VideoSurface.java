package com.twilio.signal.impl;

import org.webrtc.VideoRenderer;
import android.opengl.GLSurfaceView;
import java.util.Map;
import java.util.HashMap;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoRenderer;

import com.twilio.signal.Participant;
import com.twilio.signal.impl.VideoRendererGui;

public class VideoSurface {

	public static interface Observer {
		public void onDidAddVideoTrack();
		public void onDidRemoveVideoTrack();
		public void onDidReceiveVideoTrackEvent(VideoRenderer.I420Frame frame, String participant);
	}

	private final long nativeVideoSurface;
	private final long nativeObserver;

	private GLSurfaceView localView;
	private VideoRendererGui localVideoRendererGui;
	private GLSurfaceView remoteView;
	private VideoRendererGui remoteVideoRendererGui;

	private VideoRenderer.Callbacks localRendererCallbacks;
	private VideoRenderer.Callbacks remoteRendererCallbacks;

	private Map<Participant, GLSurfaceView> views;

	VideoSurface(long nativeVideoSurface, long nativeObserver) {
    		this.nativeVideoSurface = nativeVideoSurface;
    		this.nativeObserver = nativeObserver;
		this.views = new HashMap<Participant, GLSurfaceView>();
  	}

	/*
	 * Provide a GLSurfaceView to display the local video using the default OpenGL renderer 
	 */
	public void attachLocalView(GLSurfaceView localView) {
		this.localView = localView;
		localVideoRendererGui = new VideoRendererGui(localView, null);	
		localRendererCallbacks = localVideoRendererGui.createRenderer(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
	}

	public void attachRemoteView(GLSurfaceView remoteView) {
		this.remoteView = remoteView;
		remoteVideoRendererGui = new VideoRendererGui(remoteView, null);	
		remoteRendererCallbacks = remoteVideoRendererGui.createRenderer(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
	}

	/*
	 * Provide a GLSurfaceView to display video from a participant using the default OpenGL renderer 
	 */
	public void attachView(Participant participant, GLSurfaceView view) {
		views.put(participant, view);
	}

	public VideoRenderer.Callbacks getLocalVideoRendererCallbacks() {
		if(localRendererCallbacks == null) {
		}
		return localRendererCallbacks;
	}

	public VideoRenderer.Callbacks getRemoteVideoRendererCallbacks() {
		if(remoteRendererCallbacks == null) {
		}
		return remoteRendererCallbacks;
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
