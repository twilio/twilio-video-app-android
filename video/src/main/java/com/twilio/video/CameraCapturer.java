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

    public static CameraCapturer create(Context context,
                                        CameraSource source,
                                        CapturerErrorListener listener) {
        if (context == null) {
            throw new NullPointerException("context must not be null");
        }
        if (source == null) {
            throw new NullPointerException("source must not be null");
        }
        if (!Util.permissionGranted(context, Manifest.permission.CAMERA) &&
                listener != null) {
            listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAMERA,
                    "CAMERA permission not granted"));
        }

        // Create the webrtc capturer
        int cameraId = getCameraId(source);
        if (cameraId < 0 && listener != null) {
            listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAMERA,
                    "Invalid camera source provided"));
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

        return new CameraCapturer(webrtcVideoCapturer, listener);
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

    }

    @Override
    public void stopCapture() {

    }

    public synchronized void switchCamera() {
        // TODO: propagate error
        webrtcCapturer.switchCamera(null);
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

    private CameraCapturer(VideoCapturerAndroid webrtcCapturer, CapturerErrorListener listener) {
        this.webrtcCapturer = webrtcCapturer;
        this.listener = listener;
    }
}
