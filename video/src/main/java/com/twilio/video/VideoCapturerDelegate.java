package com.twilio.video;

import android.content.Context;

import org.webrtc.*;

import java.util.ArrayList;
import java.util.List;

class VideoCapturerDelegate implements org.webrtc.VideoCapturer {
    private final VideoCapturer videoCapturer;

    VideoCapturerDelegate(VideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        if (videoCapturer.getClass() == CameraCapturer2.class) {
            CameraCapturer2 cameraCapturer = (CameraCapturer2) videoCapturer;

            return cameraCapturer.webrtcCapturer.getSupportedFormats();
        } else {
            videoCapturer.getSupportedFormats();
        }

        return new ArrayList<CameraEnumerationAndroid.CaptureFormat>();
    }

    @Override
    public void startCapture(int width,
                             int height,
                             int framerate,
                             SurfaceTextureHelper surfaceTextureHelper,
                             Context context,
                             CapturerObserver capturerObserver) {
        if (videoCapturer.getClass() == CameraCapturer2.class) {
            CameraCapturer2 cameraCapturer = (CameraCapturer2) videoCapturer;

            cameraCapturer.webrtcCapturer.startCapture(width, height, framerate, surfaceTextureHelper, context, capturerObserver);
        } else {
            videoCapturer.startCapture(width, height, framerate, null);
        }
    }

    @Override
    public void stopCapture() throws InterruptedException {
        if (videoCapturer.getClass() == CameraCapturer2.class) {
            CameraCapturer2 cameraCapturer = (CameraCapturer2) videoCapturer;

            cameraCapturer.webrtcCapturer.stopCapture();
        } else {
            videoCapturer.stopCapture();
        }
    }

    @Override
    public void dispose() {
        if (videoCapturer.getClass() == CameraCapturer2.class) {
            CameraCapturer2 cameraCapturer = (CameraCapturer2) videoCapturer;

            cameraCapturer.webrtcCapturer.dispose();
        } else {
            // TODO
        }
    }
}
