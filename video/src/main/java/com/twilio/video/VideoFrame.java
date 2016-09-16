package com.twilio.video;

/**
 * Represents a video frame provided by a {@link CameraCapturer}.
 */
public class VideoFrame {
    /** The bytes of a frame */
    public final byte[] imageBuffer;
    /** The size of a frame */
    public final VideoDimensions dimensions;
    /** The orientation of a frame in degrees */
    public final int orientation;
    /** The time at which this frame was captured */
    public final long timestamp;

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
