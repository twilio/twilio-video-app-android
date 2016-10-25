package com.twilio.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    private static final String TAG = "ScreenCapturer";
    private static final Logger logger = Logger.getLogger(ScreenCapturer.class);

    private final static int IMAGE_READER_BUFFER_MAX = 5;
    private final static int SCREENCAPTURE_FRAME_RATE = 30;
    private final static int HD_SCREENCAST_PIXELS = 1280 * 720;

    private boolean firstFrameReported;

    private final Context context;
    private final int screenCaptureIntentResult;
    private final Intent screenCaptureIntentData;
    private final Listener screenCapturerListener;

    private VideoCapturer.Listener capturerListener;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;

    private final MediaProjection.Callback mediaProjectionCallback =
            new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                    logger.d("media projection stopped");
                }
            };

    private final VirtualDisplay.Callback virtualDisplayCallbacks = new VirtualDisplay.Callback() {
        @Override
        public void onResumed() {
            logger.d("virtual display resumed");
        }

        @Override
        public void onPaused() {
            logger.d("virtual display paused");
        }

        @Override
        public void onStopped() {
            logger.d("virtual display stopped");
        }
    };

    private final ImageReader.OnImageAvailableListener screenCapturer =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                    } catch (Exception e) {
                        String screenFrameFailure = "Failed to acquire screen frame";
                        logger.e(screenFrameFailure);
                        if (screenCapturerListener != null) {
                            screenCapturerListener.onScreenCaptureError(screenFrameFailure);
                        }
                        return;
                    }

                    if (image != null) {
                        final long captureTimeNs =
                                TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());

                        if (!firstFrameReported) {
                            if (screenCapturerListener != null) {
                                screenCapturerListener.onFirstFrameAvailable();
                            }
                            firstFrameReported = true;
                        }

                        /*
                         * Here we check for exceptions in the event that an image being
                         * read has been closed. This is not a fatal condition, so the frame
                         * will just be dropped.
                         */
                        try {
                            Image.Plane plane = image.getPlanes()[0];
                            ByteBuffer planeBuffer = plane.getBuffer();
                            int bufferSize = planeBuffer.remaining();
                            byte[] buffer = new byte[bufferSize];
                            planeBuffer.get(buffer, 0, bufferSize);

                            VideoDimensions dimensions = new VideoDimensions(image.getWidth(),
                                    image.getHeight());
                            VideoFrame videoFrame = new VideoFrame(buffer,
                                    dimensions, 0, captureTimeNs);
                            capturerListener.onFrameCaptured(videoFrame);
                            image.close();
                        } catch (Exception e) {
                            logger.e(e.getMessage());
                            if (screenCapturerListener != null) {
                                screenCapturerListener.onScreenCaptureError(e.getMessage());
                            }
                        }
                    }
                }
            };

    public interface Listener {
        void onScreenCaptureError(String errorDescription);
        void onFirstFrameAvailable();
    }

    public ScreenCapturer(Context context,
                          int screenCaptureIntentResult,
                          Intent screenCaptureIntentData,
                          Listener screenCapturerListener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            throw new RuntimeException("Screen capturing unavailable for " + Build.VERSION.SDK_INT);
        }
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        this.context = context;
        this.screenCaptureIntentResult = screenCaptureIntentResult;
        this.screenCaptureIntentData = screenCaptureIntentData;
        this.screenCapturerListener = screenCapturerListener;
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

        // Grab the media projection
        Handler handler = Util.createCallbackHandler();
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        this.mediaProjection = mediaProjectionManager
                .getMediaProjection(screenCaptureIntentResult, screenCaptureIntentData);

        // Notify user that media projection could not be accessed
        if (mediaProjection == null) {
            if (screenCapturerListener != null) {
                screenCapturerListener.onScreenCaptureError("Unable to access media projection");
            }
            capturerListener.onCapturerStarted(false);
            return;
        }
        mediaProjection.registerCallback(mediaProjectionCallback, handler);

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;

        // Instantiate an image reader in supported capturing format
        imageReader = ImageReader.newInstance(captureFormat.dimensions.width,
                captureFormat.dimensions.height,
                PixelFormat.RGBA_8888,
                IMAGE_READER_BUFFER_MAX);

        // Pipe images to our screen capturer
        imageReader.setOnImageAvailableListener(screenCapturer, handler);

        // Create our virtual display with callbacks on the current thread
        virtualDisplay = mediaProjection.createVirtualDisplay(TAG,
                captureFormat.dimensions.width,
                captureFormat.dimensions.height,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                virtualDisplayCallbacks,
                handler);
        capturerListener.onCapturerStarted(true);
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
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (mediaProjection != null) {
            mediaProjection.unregisterCallback(mediaProjectionCallback);
            mediaProjection.stop();
            mediaProjection = null;
        }
        logger.d("stopCapture done");
    }
}
