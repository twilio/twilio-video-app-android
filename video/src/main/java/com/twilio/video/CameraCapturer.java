package com.twilio.video;

import android.Manifest;
import android.content.Context;

import com.twilio.video.internal.Logger;

import org.webrtc.CameraEnumerationAndroid;
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

    final VideoCapturerAndroid webrtcCapturer;
    private final CapturerErrorListener listener;
    private CameraSource cameraSource;

    public static CameraCapturer create(Context context,
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

            return null;
        }

        // Create the webrtc capturer
        int cameraId = getCameraId(cameraSource);
        if (cameraId < 0) {
            logger.e("Failed to find camera source");
            if (listener != null) {
                listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAMERA,
                        "Unsupported camera source provided"));
            }
            return null;
        }
        CameraCapturerEventsHandler eventsHandler = new CameraCapturerEventsHandler(listener);
        VideoCapturerAndroid webrtcVideoCapturer = createVideoCapturerAndroid(cameraId,
                eventsHandler);

        if (webrtcVideoCapturer == null && listener != null) {
            listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAPTURER,
                    "Failed to create capturer"));
            return null;
        }

        return new CameraCapturer(webrtcVideoCapturer, cameraSource, listener);
    }

    @Override
    public List<CaptureFormat> getSupportedFormats() {
        return null;
    }

    @Override
    public void startCapture(int width,
                             int height,
                             int framerate,
                             VideoCapturerObserver capturerObserver) {
        // TODO: implement generic capturer interface
    }

    @Override
    public void stopCapture() {
        // TODO: implement generic capturer interface
    }

    public synchronized CameraSource getCameraSource() {
        return cameraSource;
    }

    public synchronized void switchCamera() {
        // TODO: propagate error
        webrtcCapturer.switchCamera(null);
        cameraSource = (cameraSource == CameraSource.CAMERA_SOURCE_FRONT_CAMERA) ?
                (CameraSource.CAMERA_SOURCE_BACK_CAMERA) :
                (CameraSource.CAMERA_SOURCE_FRONT_CAMERA);
    }

    private static int getCameraId(CameraSource cameraSource) {
        String deviceName;
        int cameraId = -1;

        if(cameraSource == CameraSource.CAMERA_SOURCE_BACK_CAMERA) {
            deviceName = CameraEnumerationAndroid.getNameOfBackFacingDevice();
        } else {
            deviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
        }
        if(deviceName == null) {
            cameraId = -1;
        } else {
            String[] deviceNames = CameraEnumerationAndroid.getDeviceNames();
            for(int i = 0; i < deviceNames.length; i++) {
                if(deviceName.equals(deviceNames[i])) {
                    cameraId = i;
                    break;
                }
            }
        }

        return cameraId;
    }

    private static VideoCapturerAndroid createVideoCapturerAndroid(int cameraId,
                                                                   VideoCapturerAndroid.CameraEventsHandler cameraEventsHandler) {
        String deviceName = CameraEnumerationAndroid.getDeviceName(cameraId);
        if (deviceName == null) {
            return null;
        }
        // TODO: Need to figure out the best way to get this to to webrtc
        // final EglBase.Context eglContext = EglBaseProvider.provideEglBase().getEglBaseContext();

        return VideoCapturerAndroid.create(deviceName, cameraEventsHandler);
    }

    private CameraCapturer(VideoCapturerAndroid webrtcCapturer,
                           CameraSource cameraSource,
                           CapturerErrorListener listener) {
        this.webrtcCapturer = webrtcCapturer;
        this.cameraSource = cameraSource;
        this.listener = listener;
    }
}
