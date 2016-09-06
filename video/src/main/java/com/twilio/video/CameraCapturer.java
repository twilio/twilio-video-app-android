package com.twilio.video;

import android.Manifest;
import android.content.Context;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturerAndroid;

import java.util.List;

public class CameraCapturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(CameraCapturer.class);

    /**
     * Camera source types
     */
    public enum CameraSource {
        CAMERA_SOURCE_FRONT_CAMERA,
        CAMERA_SOURCE_BACK_CAMERA
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

    @Override
    public List<VideoFormat> getSupportedFormats() {
        return formatProvider.getSupportedFormats(cameraSource);
    }

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

    public synchronized CameraSource getCameraSource() {
        return cameraSource;
    }

    public synchronized void switchCamera() {
        // TODO: propagate error
        if (webrtcCapturer != null) {
            webrtcCapturer.switchCamera(null);
        }
        cameraSource = (cameraSource == CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
                (CameraSource.CAMERA_SOURCE_BACK_CAMERA) :
                (CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
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
