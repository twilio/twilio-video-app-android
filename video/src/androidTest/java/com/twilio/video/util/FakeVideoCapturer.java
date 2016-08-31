package com.twilio.video.util;

import com.twilio.video.VideoFormat;
import com.twilio.video.VideoCapturer;
import com.twilio.video.internal.Logger;

import java.util.ArrayList;
import java.util.List;

public class FakeVideoCapturer implements VideoCapturer {
    private static final Logger logger = Logger.getLogger(FakeVideoCapturer.class);

    private VideoFormat captureFormat;
    private boolean started = false;

    public VideoFormat getCaptureFormat() {
        return captureFormat;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public List<VideoFormat> getSupportedFormats() {
        logger.i("getSupportedFormats");

        return new ArrayList<>();
    }

    @Override
    public void startCapture(VideoFormat captureFormat,
                             VideoCapturer.Listener capturerListener) {
        logger.i("startCapture");
        this.captureFormat = captureFormat;
        this.started = true;
    }

    @Override
    public void stopCapture() {
        logger.i("stopCapture");
        this.captureFormat = null;
        this.started = false;
    }
}
