package com.twilio.video;

import android.Manifest;
import android.content.Context;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturerAndroid;

import java.util.List;

/**
 * The CameraCapturer class is used to provide video frames for a {@link LocalVideoTrack} from a
 * given {@link CameraSource}. The frames are provided via the preview API of
 * {@link android.hardware.Camera}.
 *
 * <p>This class represents an implementation of a {@link VideoCapturer} interface. Although
 * public, these methods are not meant to be invoked directly.</p>
 *
 * <p><b>Note</b>: This capturer can be reused, but cannot be shared across multiple
 * {@link LocalVideoTrack}s simultaneously.</p>
 */
public class CameraCapturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(CameraCapturer.class);

    /**
     * Camera source types.
     */
    public enum CameraSource {
        FRONT_CAMERA,
        BACK_CAMERA
    }

    private final Context context;
    private final CapturerErrorListener listener;
    private final CameraCapturerFormatProvider formatProvider = new CameraCapturerFormatProvider();
    private VideoCapturerAndroid webrtcCapturer;
    private CameraSource cameraSource;
    private VideoCapturer.Listener videoCapturerListener;
    private SurfaceTextureHelper surfaceTextureHelper;
    private final org.webrtc.VideoCapturer.CapturerObserver observerAdapter =
            new org.webrtc.VideoCapturer.CapturerObserver() {
                @Override
                public void onCapturerStarted(boolean success) {
                    videoCapturerListener.onCapturerStarted(success);
                }

                @Override
                public void onByteBufferFrameCaptured(byte[] bytes,
                                                      int width,
                                                      int height,
                                                      int rotation,
                                                      long timestamp) {
                    VideoDimensions frameDimensions = new VideoDimensions(width, height);
                    VideoFrame frame = new VideoFrame(bytes, frameDimensions, rotation, timestamp);

                    videoCapturerListener.onFrameCaptured(frame);
                }

                @Override
                public void onTextureFrameCaptured(int width,
                                                   int height,
                                                   int oesTextureId,
                                                   float[] transformMatrix,
                                                   int rotation,
                                                   long timestampNs) {
                    // TODO: Do we need to support capturing to texture?
                }

                @Override
                public void onOutputFormatRequest(int width,
                                                  int height,
                                                  int framerate) {
                    // TODO: Do we need to support an output format request?
                }
            };

    public CameraCapturer(Context context,
                          CameraSource cameraSource,
                          CapturerErrorListener listener) {

        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (cameraSource == null) {
            throw new NullPointerException("camera source must not be null");
        }
        if (!Util.permissionGranted(context, Manifest.permission.CAMERA) &&
                listener != null) {
            listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAMERA,
                    "CAMERA permission not granted"));
        }
        this.context = context;
        this.cameraSource = cameraSource;
        this.listener = listener;
    }

    /**
     * Returns a list of all supported video formats. This list is based on what is specified by
     * {@link android.hardware.Camera.Parameters}, so can vary based on a device's camera
     * capabilities.
     *
     * <p><b>Note</b>: This method can be invoked for informational purposes, but is primarily used
     * internally.</p>
     *
     * @return all supported video formats.
     */
    @Override
    public List<VideoFormat> getSupportedFormats() {
        return formatProvider.getSupportedFormats(cameraSource);
    }

    /**
     * Starts capturing frames at the specified format. Frames will be provided to the given
     * listener upon availability.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.</p>
     *
     * @param captureFormat the format in which to capture frames.
     * @param videoCapturerListener consumer of available frames.
     */
    @Override
    public void startCapture(VideoFormat captureFormat,
                             VideoCapturer.Listener videoCapturerListener) {
        // Create the webrtc capturer
        this.webrtcCapturer = createVideoCapturerAndroid();
        if (webrtcCapturer == null) {
            if (listener != null) {
                listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAPTURER,
                        "Failed to create capturer"));
            }
            videoCapturerListener.onCapturerStarted(false);

            return;
        }
        this.videoCapturerListener = videoCapturerListener;

        webrtcCapturer.startCapture(captureFormat.dimensions.width,
                captureFormat.dimensions.height,
                captureFormat.framerate,
                surfaceTextureHelper,
                context,
                observerAdapter);
    }

    /**
     * Stops all frames being captured. The {@link android.hardware.Camera} interface should
     * be available for use upon completion.
     *
     * <p><b>Note</b>: This method is not meant to be invoked directly.</p>
     */
    @Override
    public void stopCapture() {
        try {
            webrtcCapturer.stopCapture();
        } catch (InterruptedException e) {
            logger.e("Failed to stop camera capturer");
        }
        webrtcCapturer.dispose();
        webrtcCapturer = null;
    }

    /**
     * Returns the currently specified camera source.
     */
    public synchronized CameraSource getCameraSource() {
        return cameraSource;
    }

    /**
     * Switches the current {@link CameraSource}. This method can be invoked while capturing frames
     * or not.
     */
    public synchronized void switchCamera() {
        // TODO: propagate error
        if (webrtcCapturer != null) {
            webrtcCapturer.switchCamera(null);
        }
        cameraSource = (cameraSource == CameraSource.FRONT_CAMERA) ?
                (CameraSource.BACK_CAMERA) :
                (CameraSource.FRONT_CAMERA);
    }

    void setSurfaceTextureHelper(SurfaceTextureHelper surfaceTextureHelper) {
        this.surfaceTextureHelper = surfaceTextureHelper;
    }

    private VideoCapturerAndroid createVideoCapturerAndroid() {
        int cameraId = CameraCapturerFormatProvider.getCameraId(cameraSource);
        if (cameraId < 0) {
            logger.e("Failed to find camera source");
            if (listener != null) {
                listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAMERA,
                        "Unsupported camera source provided"));
            }
            return null;
        }
        CameraCapturerEventsHandler eventsHandler = new CameraCapturerEventsHandler(listener);

        String deviceName = CameraEnumerationAndroid.getDeviceName(cameraId);
        if (deviceName == null) {
            return null;
        }
        // TODO: Need to figure out the best way to get this to to webrtc
        // final EglBase.Context eglContext = EglBaseProvider.provideEglBase().getEglBaseContext();

        return VideoCapturerAndroid.create(deviceName, eventsHandler);
    }
}
