/*
 * Copyright (C) 2018 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.ViewGroup;
import java.util.concurrent.CountDownLatch;
import tvi.webrtc.EglBase;
import tvi.webrtc.EglRenderer;
import tvi.webrtc.GlRectDrawer;
import tvi.webrtc.RendererCommon;
import tvi.webrtc.ThreadUtils;

public class VideoTextureView extends TextureView
        implements VideoRenderer, TextureView.SurfaceTextureListener {

    private static final Logger logger = Logger.getLogger(VideoTextureView.class);

    private final RendererCommon.VideoLayoutMeasure videoLayoutMeasure =
            new RendererCommon.VideoLayoutMeasure();
    private final EglRenderer eglRenderer;
    private VideoScaleType videoScaleType = VideoScaleType.ASPECT_FIT;
    // Callback for reporting renderer events. Read-only after initialization so no lock required.
    private RendererCommon.RendererEvents rendererEvents =
            new RendererCommon.RendererEvents() {
                @Override
                public void onFirstFrameRendered() {
                    if (listener != null) {
                        listener.onFirstFrame();
                    }
                }

                @Override
                public void onFrameResolutionChanged(
                        int videoWidth, int videoHeight, int rotation) {
                    if (listener != null) {
                        listener.onFrameDimensionsChanged(videoWidth, videoHeight, rotation);
                    }
                }
            };
    private VideoRenderer.Listener listener;

    private final Object layoutLock = new Object();
    private Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private boolean isFirstFrameRendered;
    private int rotatedFrameWidth;
    private int rotatedFrameHeight;
    private int frameRotation;
    private boolean mirror;
    // Accessed only on the main thread.
    private int surfaceWidth;
    private int surfaceHeight;

    private EglBaseProvider eglBaseProvider;

    public VideoTextureView(@NonNull Context context) {
        this(context, null);
    }

    public VideoTextureView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        String resourceName = getResourceName();
        eglRenderer = new EglRenderer(resourceName);
        setSurfaceTextureListener(this);

        TypedArray a =
                context.getTheme()
                        .obtainStyledAttributes(attrs, R.styleable.VideoTextureView, 0, 0);
        try {
            mirror = a.getBoolean(R.styleable.VideoTextureView_tviMirror, false);
            videoScaleType =
                    VideoScaleType.fromInt(
                            a.getInteger(R.styleable.VideoTextureView_tviScaleType, 0));
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Do not setup the renderer when using developer tools to avoid EGL14 runtime exceptions
        if (!isInEditMode()) {
            eglBaseProvider = EglBaseProvider.instance(this);
            init(eglBaseProvider.getRootEglBase().getEglBaseContext(), rendererEvents);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        eglRenderer.release();
        eglBaseProvider.release(this);
        super.onDetachedFromWindow();
    }

    /** Returns whether or not this view is mirroring video. */
    public boolean getMirror() {
        return mirror;
    }

    /** Sets whether or not the rendered video should be mirrored. */
    public void setMirror(final boolean mirror) {
        eglRenderer.setMirror(mirror);
        this.mirror = mirror;
        requestLayout();
    }

    /** Returns the current {@link VideoScaleType}. */
    @NonNull
    public VideoScaleType getVideoScaleType() {
        return videoScaleType;
    }

    /**
     * Sets the current scale type to specified value and updates the video.
     *
     * <p><b>Note</b>: The scale type will only be applied to dimensions defined as {@link
     * android.view.ViewGroup.LayoutParams#WRAP_CONTENT} or a custom value. Setting a width or
     * height to {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT} results in the video being
     * scaled to fill the maximum value of the dimension.
     */
    public void setVideoScaleType(@NonNull VideoScaleType scalingType) {
        ThreadUtils.checkIsOnMainThread();

        // Log warning if scale type may not be respected in certain dimensions
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null
                && (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT
                        || layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT)) {
            VideoScaleType widthScaleType =
                    (layoutParams.width == ViewGroup.LayoutParams.MATCH_PARENT)
                            ? (VideoScaleType.ASPECT_FILL)
                            : (videoScaleType);
            VideoScaleType heightScaleType =
                    (layoutParams.height == ViewGroup.LayoutParams.MATCH_PARENT)
                            ? (VideoScaleType.ASPECT_FILL)
                            : (videoScaleType);

            logger.w(
                    String.format(
                            "Scale type may not be applied as expected because "
                                    + "video view uses MATCH_PARENT. Scaling will be applied as "
                                    + "follows: width=%s, height=%s",
                            widthScaleType.name(), heightScaleType.name()));
        }

        videoLayoutMeasure.setScalingType(convertToWebRtcScaleType(scalingType));
        videoScaleType = scalingType;
        requestLayout();
    }

    /** Sets listener of rendering events. */
    public void setListener(@Nullable VideoRenderer.Listener listener) {
        this.listener = listener;
    }

    @Override
    public void renderFrame(@NonNull I420Frame frame) {
        updateFrameDimensionsAndReportEvents(frame);
        eglRenderer.renderFrame(frame.webRtcI420Frame);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        ThreadUtils.checkIsOnMainThread();
        final Point size;
        synchronized (layoutLock) {
            size =
                    videoLayoutMeasure.measure(
                            widthSpec, heightSpec, rotatedFrameWidth, rotatedFrameHeight);
        }
        setMeasuredDimension(size.x, size.y);
        logger.v("onMeasure(). New size: " + size.x + "x" + size.y);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ThreadUtils.checkIsOnMainThread();
        eglRenderer.setLayoutAspectRatio((right - left) / (float) (bottom - top));
        updateSurfaceSize();
    }

    private void init(EglBase.Context sharedContext, RendererCommon.RendererEvents rendererEvents) {
        init(sharedContext, rendererEvents, EglBase.CONFIG_PLAIN, new GlRectDrawer());
    }

    private void init(
            final EglBase.Context sharedContext,
            RendererCommon.RendererEvents rendererEvents,
            final int[] configAttributes,
            RendererCommon.GlDrawer drawer) {
        ThreadUtils.checkIsOnMainThread();
        this.rendererEvents = rendererEvents;
        synchronized (layoutLock) {
            rotatedFrameWidth = 0;
            rotatedFrameHeight = 0;
            frameRotation = 0;
        }
        eglRenderer.init(sharedContext, configAttributes, drawer);
    }

    private void updateSurfaceSize() {
        ThreadUtils.checkIsOnMainThread();
        synchronized (layoutLock) {
            if (rotatedFrameWidth != 0
                    && rotatedFrameHeight != 0
                    && getWidth() != 0
                    && getHeight() != 0) {
                final float layoutAspectRatio = getWidth() / (float) getHeight();
                final float frameAspectRatio = rotatedFrameWidth / (float) rotatedFrameHeight;
                final int drawnFrameWidth;
                final int drawnFrameHeight;
                if (frameAspectRatio > layoutAspectRatio) {
                    drawnFrameWidth = (int) (rotatedFrameHeight * layoutAspectRatio);
                    drawnFrameHeight = rotatedFrameHeight;
                } else {
                    drawnFrameWidth = rotatedFrameWidth;
                    drawnFrameHeight = (int) (rotatedFrameWidth / layoutAspectRatio);
                }
                // Aspect ratio of the drawn frame and the view is the same.
                final int width = Math.min(getWidth(), drawnFrameWidth);
                final int height = Math.min(getHeight(), drawnFrameHeight);
                logger.v(
                        "updateSurfaceSize. Layout size: "
                                + getWidth()
                                + "x"
                                + getHeight()
                                + ", frame size: "
                                + rotatedFrameWidth
                                + "x"
                                + rotatedFrameHeight
                                + ", requested surface size: "
                                + width
                                + "x"
                                + height
                                + ", old surface size: "
                                + surfaceWidth
                                + "x"
                                + surfaceHeight);
                if (width != surfaceWidth || height != surfaceHeight) {
                    surfaceWidth = width;
                    surfaceHeight = height;
                }
            } else {
                surfaceWidth = surfaceHeight = 0;
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        ThreadUtils.checkIsOnMainThread();
        eglRenderer.createEglSurface(surfaceTexture);
        surfaceWidth = width;
        surfaceHeight = height;
        updateSurfaceSize();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        ThreadUtils.checkIsOnMainThread();
        final CountDownLatch completionLatch = new CountDownLatch(1);
        eglRenderer.releaseEglSurface(completionLatch::countDown);
        ThreadUtils.awaitUninterruptibly(completionLatch);
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        ThreadUtils.checkIsOnMainThread();
        logger.v("surfaceChanged: size: " + width + "x" + height);
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        ThreadUtils.checkIsOnMainThread();
        logger.v("onSurfaceTextureUpdated");
    }

    private String getResourceName() {
        try {
            return getResources().getResourceEntryName(getId()) + ": ";
        } catch (Resources.NotFoundException e) {
            return "";
        }
    }

    // Update frame dimensions and report any changes to |rendererEvents|.
    private void updateFrameDimensionsAndReportEvents(I420Frame frame) {
        synchronized (layoutLock) {
            if (!isFirstFrameRendered) {
                isFirstFrameRendered = true;
                logger.v("Reporting first rendered frame.");
                if (rendererEvents != null) {
                    rendererEvents.onFirstFrameRendered();
                }
            }
            if (rotatedFrameWidth != frame.rotatedWidth()
                    || rotatedFrameHeight != frame.rotatedHeight()
                    || frameRotation != frame.rotationDegree) {
                logger.v(
                        "Reporting frame resolution changed to "
                                + frame.width
                                + "x"
                                + frame.height
                                + " with rotation "
                                + frame.rotationDegree);
                if (rendererEvents != null) {
                    rendererEvents.onFrameResolutionChanged(
                            frame.width, frame.height, frame.rotationDegree);
                }
                rotatedFrameWidth = frame.rotatedWidth();
                rotatedFrameHeight = frame.rotatedHeight();
                frameRotation = frame.rotationDegree;
                uiThreadHandler.post(
                        () -> {
                            updateSurfaceSize();
                            requestLayout();
                        });
            }
        }
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
}
