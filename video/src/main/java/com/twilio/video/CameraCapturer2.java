package com.twilio.video;

import org.webrtc.VideoCapturerAndroid;

import java.util.List;

public class CameraCapturer2 implements VideoCapturer {
    final VideoCapturerAndroid webrtcCapturer;

    public static CameraCapturer2 create() {
        VideoCapturerAndroid videoCapturerAndroid =
                VideoCapturerAndroid.create("nasdfasdf", null, false);

        return new CameraCapturer2(videoCapturerAndroid);
    }

    public CameraCapturer2(VideoCapturerAndroid webrtcCapturer) {
        this.webrtcCapturer = webrtcCapturer;
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
}
