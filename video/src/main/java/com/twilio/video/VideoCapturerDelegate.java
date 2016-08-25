package com.twilio.video;

import android.content.Context;

import org.webrtc.*;

import java.util.ArrayList;
import java.util.List;

final class VideoCapturerDelegate implements org.webrtc.VideoCapturer {
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
        // TODO: ugh this is still cheating..need to figure out a way to pass this better
        if (videoCapturer instanceof CameraCapturer) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.setSurfaceTextureHelper(surfaceTextureHelper);
        }
        videoCapturer.startCapture(width,
                height,
                framerate,
                new VideoCapturerObserverAdapter(capturerObserver));
    }

    @Override
    public void stopCapture() throws InterruptedException {
        videoCapturer.stopCapture();
    }

    @Override
    public void dispose() {
        // Currently this is not part of our capturer api so we can just ignore
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
