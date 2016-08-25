package com.twilio.video;

public class VideoFrame {
    public final byte[] imageBuffer;
    public final VideoDimensions dimensions;
    public final int orientation;
    public long timestamp;

    public VideoFrame(byte[] imageBuffer,
                      VideoDimensions dimensions,
                      int orientation,
                      long timestamp) {
        this.imageBuffer = imageBuffer;
        this.dimensions = dimensions;
        this.orientation = orientation;
        this.timestamp = timestamp;
    }
}
