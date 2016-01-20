package com.twilio.conversations;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import com.twilio.conversations.impl.I420Frame;

import org.webrtc.SurfaceViewRenderer;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.RendererCommon.RendererEvents;

/**
 * A VideoViewRenderer receives frames from a {@link VideoTrack} and
 * renders them to a view.
 *
 */
public class VideoViewRenderer implements VideoRenderer {
    // Used to ensure that our renderer has a means to post to main thread
    // for renderering events
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    private final RendererEvents internalEventListener = new RendererEvents() {
        @Override
        public void onFirstFrameRendered() {
            refreshRenderer();
            if (rendererObserver != null) {
                rendererObserver.onFirstFrame();
            }
        }

        @Override
        public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
            refreshRenderer();
            if (rendererObserver != null) {
                rendererObserver.onFrameDimensionsChanged(videoWidth, videoHeight, rotation);
            }
        }
    };

    private final SurfaceViewRenderer surfaceViewRenderer;
    private boolean mirror;
    private VideoRendererObserver rendererObserver;

	/**
	 * Create a video view renderer that will display frames in
	 * the provided container
	 * @param context Activity context
	 * @param container The view where the frames should be rendered
	 */
	public VideoViewRenderer(Context context,
                             ViewGroup container) {
        this.surfaceViewRenderer = new SurfaceViewRenderer(context);
        setupRenderer(context, container);
	}

    public boolean getMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        surfaceViewRenderer.setMirror(mirror);
        refreshRenderer();
    }

    public void setObserver(VideoRendererObserver rendererObserver) {
        this.rendererObserver = rendererObserver;
    }

    private void setupRenderer(final Context context, final ViewGroup container) {
		container.addView(surfaceViewRenderer);
        surfaceViewRenderer.init(EglBaseProvider.provideEglBase().getContext(),
                internalEventListener);
                surfaceViewRenderer.setScalingType(ScalingType.SCALE_ASPECT_FIT);
	}

	@Override
	public void renderFrame(I420Frame i420) {
        surfaceViewRenderer.renderFrame(i420.frame);
	}

    private void refreshRenderer() {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (surfaceViewRenderer != null) {
                    surfaceViewRenderer.requestLayout();
                }
            }
        });
    }
}
