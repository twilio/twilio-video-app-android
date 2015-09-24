package com.twilio.example.quickstart;

import com.twilio.signal.VideoRendererGui;
import com.twilio.signal.VideoRenderer;
import com.twilio.signal.I420Frame;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.util.Log;


public class VideoRendererGuiAdapter implements VideoRenderer {
	private org.webrtc.VideoRenderer.Callbacks rendererCallbacks;

	public VideoRendererGuiAdapter(Context context, ViewGroup container) {
		createRenderer(context, container);
	}

	private void createRenderer(final Context context, final ViewGroup container) {
		container.post(new Runnable() {

			@Override
			public void run() {
				GLSurfaceView view = new GLSurfaceView(context);
				container.addView(view);
				final VideoRendererGui videoRendererGui = new VideoRendererGui(view, null);
				rendererCallbacks = videoRendererGui.createRenderer(0,0,100,100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
			}

		});

	}

	@Override
	public void setSize(int width, int height) {
		Log.i("VideoRendererGuiAdapter", "setSize " + width + " " + height);
		if(rendererCallbacks != null) {

		}
	}

	@Override
	public void renderFrame(I420Frame frame) {
		Log.i("VideoRendererGuiAdapter", "renderFrame");
		if(rendererCallbacks != null) {
			rendererCallbacks.renderFrame(frame.getRawFrame());
		}
	}

}
