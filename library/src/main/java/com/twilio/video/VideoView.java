/*
 * Copyright (C) 2017 Twilio, Inc.
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
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

/**
 * A VideoView renders frames from a {@link VideoTrack}. This class is an extension of {@link
 * android.view.SurfaceView}, so it can be placed in your XML view hierarchy.
 */
public class VideoView extends SurfaceViewRenderer implements VideoRenderer {
  private static final Logger logger = Logger.getLogger(VideoView.class);

  // Used to ensure that our renderer has a means to post to main thread for renderer events
  private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
  private final RendererCommon.RendererEvents internalEventListener =
      new RendererCommon.RendererEvents() {
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
  private EglBaseProvider eglBaseProvider;

  public VideoView(Context context) {
    this(context, null);
  }

  public VideoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VideoView, 0, 0);

    try {
      mirror = a.getBoolean(R.styleable.VideoView_mirror, false);
      videoScaleType = VideoScaleType.fromInt(a.getInteger(R.styleable.VideoView_scaleType, 0));
      overlaySurface = a.getBoolean(R.styleable.VideoView_overlaySurface, false);
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
      setupRenderer();
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.release();
    this.eglBaseProvider.release(this);
    super.onDetachedFromWindow();
  }

  /** Returns whether or not this view is mirroring video. */
  public boolean getMirror() {
    return mirror;
  }

  /** Sets whether or not the rendered video should be mirrored. */
  public void setMirror(boolean mirror) {
    this.mirror = mirror;
    super.setMirror(mirror);
    refreshRenderer();
  }

  /** Returns the current {@link VideoScaleType}. */
  public VideoScaleType getVideoScaleType() {
    return videoScaleType;
  }

  /**
   * Sets the current scale type to specified value and updates the video.
   *
   * <p><b>Note</b>: The scale type will only be applied to dimensions defined as {@link
   * android.view.ViewGroup.LayoutParams#WRAP_CONTENT} or a custom value. Setting a width or height
   * to {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT} results in the video being scaled
   * to fill the maximum value of the dimension.
   */
  public void setVideoScaleType(VideoScaleType videoScaleType) {
    ViewGroup.LayoutParams layoutParams = getLayoutParams();

    // Log warning if scale type may not be respected in certain dimensions
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

    this.videoScaleType = videoScaleType;
    setScalingType(convertToWebRtcScaleType(videoScaleType));
    refreshRenderer();
  }

  /** Sets listener of rendering events. */
  public void setListener(VideoRenderer.Listener listener) {
    this.listener = listener;
  }

  @Override
  public void renderFrame(I420Frame frame) {
    super.renderFrame(frame.webRtcI420Frame);
  }

  /**
   * Controls placement of the video render relative to other surface.
   *
   * @param overlaySurface if true, video renderer is placed on top of another video renderer in the
   *     window (but still behind window itself).
   */
  public void applyZOrder(boolean overlaySurface) {
    this.overlaySurface = overlaySurface;
    setZOrderMediaOverlay(overlaySurface);
  }

  private void setupRenderer() {
    init(eglBaseProvider.getRootEglBase().getEglBaseContext(), internalEventListener);
    setMirror(mirror);
    setScalingType(convertToWebRtcScaleType(videoScaleType));
    setZOrderMediaOverlay(overlaySurface);
  }

  private void refreshRenderer() {
    uiThreadHandler.post(
        new Runnable() {
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
}
