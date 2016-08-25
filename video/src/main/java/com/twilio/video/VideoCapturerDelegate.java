package com.twilio.video;

import android.content.Context;

import org.webrtc.*;

import java.util.ArrayList;
import java.util.List;

/*
 * WIP: Delegates callbacks from WebRTC to our {@link VideoCapturer}
 */
class VideoCapturerDelegate implements org.webrtc.VideoCapturer {
    private final VideoCapturer videoCapturer;

    VideoCapturerDelegate(VideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        if (videoCapturer.getClass() == CameraCapturer.class) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

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
        if (videoCapturer.getClass() == CameraCapturer.class) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.webrtcCapturer.startCapture(width,
                    height,
                    framerate,
                    surfaceTextureHelper,
                    context,
                    capturerObserver);
        } else {
            videoCapturer.startCapture(width, height, framerate, null);
        }
    }

    @Override
    public void stopCapture() throws InterruptedException {
        if (videoCapturer.getClass() == CameraCapturer.class) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.webrtcCapturer.stopCapture();
        } else {
            videoCapturer.stopCapture();
        }
    }

    @Override
    public void dispose() {
        if (videoCapturer.getClass() == CameraCapturer.class) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.webrtcCapturer.dispose();
        } else {
            // TODO: Are we going to publish a release concept on the public capturer api?
        }
    }
}
