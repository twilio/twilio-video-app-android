package com.twilio.video;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public class VideoView extends SurfaceViewRenderer implements VideoRenderer {
    // Used to ensure that our renderer has a means to post to main thread
    // for renderer events
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());

    private final RendererCommon.RendererEvents internalEventListener = new RendererCommon.RendererEvents() {
        @Override
        public void onFirstFrameRendered() {
            refreshRenderer();
            if (listener != null) {
                listener.onFirstFrame();
            }
        }

        @Override
        public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
            refreshRenderer();
            if (listener != null) {
                listener.onFrameDimensionsChanged(videoWidth, videoHeight, rotation);
            }
        }
    };

    private boolean mirror = false;
    private boolean overlaySurface = false;
    private VideoScaleType videoScaleType = VideoScaleType.ASPECT_FIT;
    private VideoRenderer.Listener listener;

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.VideoView, 0, 0);

        try {
            mirror = a.getBoolean(R.styleable.VideoView_mirror, false);
            videoScaleType = VideoScaleType.fromInt(a.getInteger(R.styleable.VideoView_scaleType,
                    0));
            overlaySurface = a.getBoolean(R.styleable.VideoView_overlaySurface, false);
        } finally {
            a.recycle();
        }

        setupRenderer();
    }

    public boolean getMirror() {
        return mirror;
    }

    public void setMirror(boolean mirror) {
        this.mirror = mirror;
        super.setMirror(mirror);
        refreshRenderer();
    }

    public VideoScaleType getVideoScaleType() {
        return videoScaleType;
    }

    public void setVideoScaleType(VideoScaleType videoScaleType) {
        this.videoScaleType = videoScaleType;
        setScalingType(convertToWebRtcScaleType(videoScaleType));
        refreshRenderer();
    }

    public void setListener(VideoRenderer.Listener listener) {
        this.listener = listener;
    }

    /**
     * Releases resources associated with the video renderer
     */
    public void release() {
        super.release();
    }

    @Override
    public void renderFrame(I420Frame frame) {
        super.renderFrame(convertToWebRtcFrame(frame));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * Controls placement of the video render relative to other surface
     *
     * @param overlaySurface if true, video renderer is placed on top of another video renderer
     *                       in the window (but still behind window itself)
     */
    public void applyZOrder(boolean overlaySurface) {
        this.overlaySurface = overlaySurface;
        setZOrderMediaOverlay(overlaySurface);
    }

    private void setupRenderer() {
        init(EglBaseProvider.provideEglBase().getEglBaseContext(),
                internalEventListener);
        setMirror(mirror);
        setScalingType(convertToWebRtcScaleType(videoScaleType));
        setZOrderMediaOverlay(overlaySurface);
    }

    private void refreshRenderer() {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    private RendererCommon.ScalingType convertToWebRtcScaleType(VideoScaleType videoScaleType) {
        switch (videoScaleType) {
            case ASPECT_FIT:
                return RendererCommon.ScalingType.SCALE_ASPECT_FIT;
            case ASPECT_FILL:
                return RendererCommon.ScalingType.SCALE_ASPECT_FILL;
            case ASPECT_BALANCED:
                return RendererCommon.ScalingType.SCALE_ASPECT_BALANCED;
            default:
                return RendererCommon.ScalingType.SCALE_ASPECT_FIT;
        }
    }

    private org.webrtc.VideoRenderer.I420Frame convertToWebRtcFrame(I420Frame frame) {
        try {
            Constructor<org.webrtc.VideoRenderer.I420Frame> i420FrameConstructor =
                    org.webrtc.VideoRenderer.I420Frame.class
                            .getDeclaredConstructor(int.class,
                                    int.class,
                                    int.class,
                                    int[].class,
                                    ByteBuffer[].class,
                                    long.class);
            i420FrameConstructor.setAccessible(true);
            return i420FrameConstructor.newInstance(frame.width,
                    frame.height,
                    frame.rotationDegree,
                    frame.yuvStrides,
                    frame.yuvPlanes,
                    frame.nativeFramePointer);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Unable to transform I420 frame");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to transform I420 frame");
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to transform I420 frame");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to transform I420 frame");
        }
    }
}
