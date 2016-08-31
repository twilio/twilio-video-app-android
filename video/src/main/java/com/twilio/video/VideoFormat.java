package com.twilio.video;

public class VideoFormat {
    public final VideoDimensions dimensions;
    public final int framerate;
    public final VideoPixelFormat pixelFormat;

    public VideoFormat(VideoDimensions dimensions,
                       int framerate,
                       VideoPixelFormat pixelFormat) {
        this.dimensions = dimensions;
        this.framerate = framerate;
        this.pixelFormat = pixelFormat;
    }
}
