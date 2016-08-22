package com.twilio.video.util;

import com.twilio.video.CaptureFormat;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoCapturerObserver;
import com.twilio.video.internal.Logger;

import java.util.List;

public class FakeVideoCapturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(FakeVideoCapturer.class);

    private int captureWidth = 0;
    private int captureHeight = 0;
    private int captureFramerate = 0;

    public int getCaptureWidth() {
        return captureWidth;
    }

    public int getCaptureHeight() {
        return captureHeight;
    }

    public int getCaptureFramerate() {
        return captureFramerate;
    }

    @Override
    public List<CaptureFormat> getSupportedFormats() {
        logger.i("getSupportedFormats");

        return null;
    }

    @Override
    public void startCapture(int width,
                             int height,
                             int framerate,
                             VideoCapturerObserver capturerObserver) {
        logger.i("startCapture");
        this.captureWidth = width;
        this.captureHeight = height;
        this.captureFramerate = framerate;
    }

    @Override
    public void stopCapture() {
        logger.i("stopCapture");
    }
}
