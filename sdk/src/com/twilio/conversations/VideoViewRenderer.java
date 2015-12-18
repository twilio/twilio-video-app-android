package com.twilio.conversations;

import com.twilio.conversations.impl.VideoRendererGui;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.ViewGroup;

/**
 * A VideoViewRenderer receives frames from a {@link VideoTrack} and
 * renders them to a view.
 *
 */
public class VideoViewRenderer implements VideoRenderer {
	private VideoRenderer videoRenderer;
	private GLSurfaceView videoView;

	/**
	 * Create a video view renderer that will display frames in
	 * the provided container
	 * @param context Activity context
	 * @param container The view where the frames should be rendered
	 */
	public VideoViewRenderer(Context context, ViewGroup container) {
		setupRenderer(context, container);
	}

	private void setupRenderer(final Context context, final ViewGroup container) {
		videoView = new GLSurfaceView(context);
		container.addView(videoView);
		VideoRendererGui videoRendererGui = new VideoRendererGui(videoView, null);
		videoRenderer = videoRendererGui.createRenderer(0,0,100,100, VideoRendererGui.ScalingType.SCALE_ASPECT_FIT, true);
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
		if(videoRenderer != null) {
			videoRenderer.setObserver(observer);
		}
	}

	/**
	 * Resumes rendering to the view
	 *
	 */
	public void onResume() {
		if(videoView != null) {
			videoView.onResume();
		}
	}

	/**
	 * Pauses rendering to the view
	 *
	 */
	public void onPause() {
		if(videoView != null) {
			videoView.onPause();
		}
	}

}
