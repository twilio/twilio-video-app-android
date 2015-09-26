package com.twilio.signal;

import com.twilio.signal.VideoRenderer;
import com.twilio.signal.VideoViewRenderer;
import com.twilio.signal.I420Frame;
import com.twilio.signal.impl.VideoRendererGui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;
import android.util.Log;


public class VideoViewRenderer implements VideoRenderer {
	private VideoRenderer videoRenderer;
	private VideoRendererObserver observer;
	private GLSurfaceView videoView;

	public VideoViewRenderer(Context context, ViewGroup container) {
		setupRenderer(context, container);
	}

	private void setupRenderer(final Context context, final ViewGroup container) {
		container.post(new Runnable() {

			@Override
			public void run() {
				GLSurfaceView videoView = new GLSurfaceView(context);
				container.addView(videoView);
				final VideoRendererGui videoRendererGui = new VideoRendererGui(videoView, null);
				videoRenderer = videoRendererGui.createRenderer(0,0,100,100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
				videoRenderer.setObserver(observer);
			}

		});

	}

	@Override
	public void setSize(int width, int height) {
		if(videoRenderer != null) {
			videoRenderer.setSize(width, height);
		}
	}

	@Override
	public void renderFrame(I420Frame frame) {
		if(videoRenderer != null) {
			videoRenderer.renderFrame(frame);
		}
	}

	@Override
	public void setObserver(VideoRendererObserver observer) {
		this.observer = observer;
		if(videoRenderer != null) {
			videoRenderer.setObserver(observer);
		}
	}

       public void onResume() {
               if(videoView != null) {
                       videoView.onResume();
               }
       }

       public void onPause() {
               if(videoView != null) {
                       videoView.onPause();
               }
       }
 
}
