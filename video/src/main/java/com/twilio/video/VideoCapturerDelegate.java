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
        return convertToWebRtcFormats(videoCapturer.getSupportedFormats());
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
        videoCapturer.stopCapture();
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

    private List<CameraEnumerationAndroid.CaptureFormat> convertToWebRtcFormats(List<CaptureFormat> captureFormats) {
        List<CameraEnumerationAndroid.CaptureFormat> webRtcCaptureFormats =
                new ArrayList<>(captureFormats.size());

        for (int i = 0 ; i < captureFormats.size() ; i++) {
            CaptureFormat captureFormat = captureFormats.get(i);
            CameraEnumerationAndroid.CaptureFormat webRtcCaptureFormat =
                    new CameraEnumerationAndroid.CaptureFormat(captureFormat.getWidth(),
                            captureFormat.getHeight(),
                            captureFormat.getMinFramerate(),
                            captureFormat.getMaxFramerate(),
                            captureFormat.getCapturePixelFormat().getValue());

            webRtcCaptureFormats.add(i, webRtcCaptureFormat);
        }

        return webRtcCaptureFormats;
    }
}
