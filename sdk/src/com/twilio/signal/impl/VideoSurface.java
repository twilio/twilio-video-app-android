package com.twilio.signal.impl;

import org.webrtc.VideoRenderer;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.content.Context;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoRenderer;

import com.twilio.signal.Participant;
import com.twilio.signal.impl.VideoRendererGui;
import com.twilio.signal.impl.TrackInfo;
import com.twilio.signal.impl.logging.Logger;

/*
 * The VideoSurface creates a video renderer for any container that requires frame rendering 
 */
public class VideoSurface {

	public static interface Observer {
		public void onDidAddVideoTrack(TrackInfo trackInfo);
		public void onDidRemoveVideoTrack(TrackInfo trackInfo);
		public void onDidReceiveVideoTrackEvent(VideoRenderer.I420Frame frame, TrackInfo trackInfo);
	}

	static final Logger logger = Logger.getLogger(VideoSurface.class);

	private final Context context;
	private final long nativeVideoSurface;
	private final long nativeObserver;

	private volatile Map<ViewGroup, VideoRenderer.Callbacks> rendererCallbacks;
	private volatile Set<ViewGroup> creatingRenderer;

	VideoSurface(Context context, long nativeVideoSurface, long nativeObserver) {
		this.context = context;
    		this.nativeVideoSurface = nativeVideoSurface;
    		this.nativeObserver = nativeObserver;
		this.rendererCallbacks = new HashMap<ViewGroup, VideoRenderer.Callbacks>();
		this.creatingRenderer = new HashSet<ViewGroup>();
  	}

	private void createRenderer(final ViewGroup container) {
		creatingRenderer.add(container);
		// GLSurfaceView must be created on the UI Thread. 
		container.post(new Runnable() {
			@Override
			public void run() {
				GLSurfaceView view = new GLSurfaceView(context);
				container.addView(view);
				final VideoRendererGui videoRendererGui = new VideoRendererGui(view, null);
				VideoRenderer.Callbacks rendererCallback = videoRendererGui.createRenderer(0, 0, 100, 100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
				rendererCallbacks.put(container, rendererCallback);
			}
		});
	}

	public void renderFrame(VideoRenderer.I420Frame frame, ViewGroup container) {
		logger.i("renderFrame");
		if(frame == null) {
			logger.i("Frame is null");
			return;
		}
		if(container == null) {
			logger.i("View is null");
			return;
		}

		VideoRenderer.Callbacks rendererCallback = rendererCallbacks.get(container);
		if(rendererCallback != null) {
			rendererCallback.renderFrame(frame);
		} else if(rendererCallback == null && !creatingRenderer.contains(container)) {
			createRenderer(container);
		}
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
