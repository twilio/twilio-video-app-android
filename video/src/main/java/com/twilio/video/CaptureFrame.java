package com.twilio.video;

public class CaptureFrame {
    private final byte[] imageBuffer;
    private final VideoDimensions dimensions;
    private final int orientation;
    private long timestamp;

    public CaptureFrame(byte[] imageBuffer,
                        VideoDimensions dimensions,
                        int orientation,
                        long timestamp) {
        this.imageBuffer = imageBuffer;
        this.dimensions = dimensions;
        this.orientation = orientation;
        this.timestamp = timestamp;
    }

    public byte[] getImageBuffer() {
        return imageBuffer;
    }

    public VideoDimensions getDimensions() {
        return dimensions;
    }

    public int getOrientation() {
        return orientation;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
