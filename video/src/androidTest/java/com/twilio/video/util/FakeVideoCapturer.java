package com.twilio.video.util;

import com.twilio.video.VideoDimensions;
import com.twilio.video.VideoFormat;
import com.twilio.video.VideoCapturer;
import com.twilio.video.VideoPixelFormat;
import com.twilio.video.Logger;

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
        VideoDimensions dimensions = new VideoDimensions(640, 360);
        VideoFormat videoFormat = new VideoFormat(dimensions, 30, VideoPixelFormat.RGBA_8888);
        List<VideoFormat> supportedFormats = new ArrayList<>();

        supportedFormats.add(videoFormat);

        return supportedFormats;
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
