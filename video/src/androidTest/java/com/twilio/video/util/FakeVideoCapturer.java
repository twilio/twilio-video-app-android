package com.twilio.video.util;

import com.twilio.video.VideoFormat;
import com.twilio.video.VideoCapturer;
import com.twilio.video.internal.Logger;

import java.util.List;

public class FakeVideoCapturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(FakeVideoCapturer.class);

    private int captureWidth = 0;
    private int captureHeight = 0;
    private int captureFramerate = 0;
    private boolean started = false;

    public int getCaptureWidth() {
        return captureWidth;
    }

    public int getCaptureHeight() {
        return captureHeight;
    }

    public int getCaptureFramerate() {
        return captureFramerate;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public List<VideoFormat> getSupportedFormats() {
        logger.i("getSupportedFormats");

        return null;
    }

    @Override
    public void startCapture(int width,
                             int height,
                             int framerate,
                             VideoCapturer.Listener capturerListener) {
        logger.i("startCapture");
        this.captureWidth = width;
        this.captureHeight = height;
        this.captureFramerate = framerate;
        this.started = true;
    }

    @Override
    public void stopCapture() {
        logger.i("stopCapture");
        this.captureWidth = 0;
        this.captureHeight = 0;
        this.captureFramerate = 0;
        this.started = false;
    }
}
