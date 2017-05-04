package com.twilio.video;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.SurfaceTextureHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * The ScreenCapturer class is used to provide video frames for a {@link LocalVideoTrack} from a
 * device's screen. The frames are provided via the {@link MediaProjection} api. This capturer
 * is only compatible with {@link android.os.Build.VERSION_CODES#LOLLIPOP} or higher.
 *
 * <p>This class represents an implementation of a {@link VideoCapturer} interface. Although
 * public, these methods are not meant to be invoked directly.</p>
 *
 * <p><b>Note</b>: This capturer can be reused, but cannot be shared across multiple
 * {@link LocalVideoTrack}s simultaneously.</p>
 */
@TargetApi(21)
public class ScreenCapturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(ScreenCapturer.class);

    private final static int SCREENCAPTURE_FRAME_RATE = 30;

    private boolean firstFrameReported;

    private final Context context;
    private final Intent screenCaptureIntentData;
    private final Listener screenCapturerListener;
    private final int screenCaptureIntentResult;
    private final Handler listenerHandler;

    private VideoCapturer.Listener capturerListener;
    private ScreenCapturerAndroid webRtcScreenCapturer;
    private SurfaceTextureHelper surfaceTextureHelper;

    private final MediaProjection.Callback mediaProjectionCallback =
            new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                    logger.d("media projection stopped");
                }
            };

    private final org.webrtc.VideoCapturer.CapturerObserver observerAdapter =
        new org.webrtc.VideoCapturer.CapturerObserver() {
            @Override
            public void onCapturerStarted(boolean success) {
                logger.d("screen capturer started");
                if (!success) {
                    if (screenCapturerListener != null) {
                        listenerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                screenCapturerListener.onScreenCaptureError(
                                    "Failed to start screen capturer");
                            }
                        });
                    }
                }
                capturerListener.onCapturerStarted(success);
            }

            @Override
            public void onCapturerStopped() {
                logger.d("screen capturer stopped");
            }

            @Override
            public void onByteBufferFrameCaptured(
                byte[] data, int width, int height, int rotation, long timeStamp) {
                // Not used in screen capturer
            }

            @Override
            public void onTextureFrameCaptured(int width, int height, int oesTextureId,
                                               float[] transformMatrix, int rotation,
                                               long timestamp) {
                if (!firstFrameReported) {
                    if (screenCapturerListener != null) {
                        listenerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                screenCapturerListener.onFirstFrameAvailable();
                            }
                        });
                    }
                    firstFrameReported = true;
                }
                VideoDimensions frameDimensions = new VideoDimensions(width, height);
                VideoFrame frame = new VideoFrame(oesTextureId,
                    transformMatrix,
                    frameDimensions,
                    VideoFrame.RotationAngle.fromInt(rotation),
                    timestamp);

                capturerListener.onFrameCaptured(frame);
            }
        };

    public interface Listener {
        void onScreenCaptureError(String errorDescription);
        void onFirstFrameAvailable();
    }

    public ScreenCapturer(@NonNull Context context,
                          int screenCaptureIntentResult,
                          @NonNull Intent screenCaptureIntentData,
                          @Nullable Listener screenCapturerListener) {
        Preconditions.checkState(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP,
                "Screen capturing unavailable for " + Build.VERSION.SDK_INT);
        Preconditions.checkNotNull(context, "context must not be null");
        Preconditions.checkNotNull(screenCaptureIntentData, "intent must not be null");

        this.context = context;
        this.screenCaptureIntentData = screenCaptureIntentData;
        this.screenCaptureIntentResult = screenCaptureIntentResult;
        this.screenCapturerListener = screenCapturerListener;
        this.listenerHandler = Util.createCallbackHandler();
    }

    /**
     * Returns a list of all supported video formats. This is currently limited to VGA and 720p at
     * 30 frames per second with a pixel format of RGBA.
     *
     * <p><b>Note</b>: This method can be invoked for informational purposes, but is primarily used
     * internally.</p>
     *
     * @return all supported video formats.
     */
    @Override
    public List<VideoFormat> getSupportedFormats() {
        // TODO: Add support for more formats based on the size of device screen
        List<VideoFormat> screencastFormats = new ArrayList<>();
        VideoDimensions vgaDimensions = new VideoDimensions(640, 480);
        VideoDimensions hdDimensions = new VideoDimensions(1280, 720);

        screencastFormats.add(new VideoFormat(vgaDimensions,
                SCREENCAPTURE_FRAME_RATE, VideoPixelFormat.RGBA_8888));
        screencastFormats.add(new VideoFormat(hdDimensions,
                SCREENCAPTURE_FRAME_RATE, VideoPixelFormat.RGBA_8888));

        return screencastFormats;
    }

    /**
     * Inidicates that the screen capturer is a screencast.
     */
    @Override
    public boolean isScreencast() {
        return true;
    }

    /**
     * Starts capturing frames at the specified format. Frames will be provided to the given
     * listener upon availability.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.</p>
     *
     * @param captureFormat the format in which to capture frames.
     * @param capturerListener consumer of available frames.
     */
    @Override
    public void startCapture(VideoFormat captureFormat, VideoCapturer.Listener capturerListener) {
        this.capturerListener = capturerListener;
        this.firstFrameReported = false;
        if (screenCaptureIntentResult != Activity.RESULT_OK) {
            if (screenCapturerListener != null) {
                listenerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        screenCapturerListener.onScreenCaptureError(
                            "MediaProjection permissions must be granted to start ScreenCapturer");
                    }
                });
            }
            capturerListener.onCapturerStarted(false);
            return;
        }
        this.webRtcScreenCapturer =
            new ScreenCapturerAndroid(screenCaptureIntentData, mediaProjectionCallback);
        webRtcScreenCapturer.initialize(surfaceTextureHelper, context, observerAdapter);
        webRtcScreenCapturer.startCapture(captureFormat.dimensions.width,
            captureFormat.dimensions.height,
            captureFormat.framerate);

    }

    /**
     * Stops all frames being captured. {@link MediaProjection} should be available for use
     * upon completion.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.</p>
     */
    @Override
    public void stopCapture() {
        logger.d("stopCapture");
        if (webRtcScreenCapturer != null) {
            webRtcScreenCapturer.stopCapture();
            webRtcScreenCapturer.dispose();
            webRtcScreenCapturer = null;
        }
        logger.d("stopCapture done");
    }

    void setSurfaceTextureHelper(SurfaceTextureHelper surfaceTextureHelper) {
        this.surfaceTextureHelper = surfaceTextureHelper;
    }
}
