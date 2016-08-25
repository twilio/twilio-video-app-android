package com.twilio.video;

public class VideoFormat {
    public final int width;
    public final int height;
    public final int maxFramerate;
    public final int minFramerate;
    public final VideoPixelFormat videoPixelFormat;

    public VideoFormat(int width,
                       int height,
                       int minFramerate,
                       int maxFramerate,
                       VideoPixelFormat videoPixelFormat) {
        this.width = width;
        this.height = height;
        this.minFramerate = minFramerate;
        this.maxFramerate = maxFramerate;
        this.videoPixelFormat = videoPixelFormat;
    }
}
