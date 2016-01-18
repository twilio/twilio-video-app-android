package com.twilio.conversations;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import com.twilio.conversations.impl.I420Frame;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon.ScalingType;

/**
 * A VideoViewRenderer receives frames from a {@link VideoTrack} and
 * renders them to a view.
 *
 */
public class VideoViewRenderer implements VideoRenderer {
    // TODO aalaniz - I am not a huge fan of this but trying to just get something off the ground
    private static final EglBase rootEglBase = new EglBase();

    private SurfaceViewRenderer surfaceViewRenderer;
    private boolean firstFrameRendered = false;

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
		surfaceViewRenderer = new SurfaceViewRenderer(context);
		container.addView(surfaceViewRenderer);
        surfaceViewRenderer.init(rootEglBase.getContext(), null);
        surfaceViewRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
        surfaceViewRenderer.requestLayout();
	}

	@Override
	public void renderFrame(I420Frame i420) {
		if(surfaceViewRenderer != null) {
            if (!firstFrameRendered) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        surfaceViewRenderer.invalidate();
                        surfaceViewRenderer.requestLayout();
                    }
                });
            }
			surfaceViewRenderer.renderFrame(i420.frame);
            firstFrameRendered = true;
		}
	}
}
