package com.twilio.video;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Represents a video frame provided by a {@link CameraCapturer}.
 */
public class VideoFrame {
    @Retention(SOURCE)
    @IntDef({ROTATION_ANGLE_0_DEGREES,
        ROTATION_ANGLE_90_DEGREES,
        ROTATION_ANGLE_180_DEGREES,
        ROTATION_ANGLE_270_DEGREES})
    public @interface RotationAngle {}
    public static final int ROTATION_ANGLE_0_DEGREES = 0;
    public static final int ROTATION_ANGLE_90_DEGREES = 90;
    public static final int ROTATION_ANGLE_180_DEGREES = 180;
    public static final int ROTATION_ANGLE_270_DEGREES = 270;


    /** The bytes of a frame. */
    public final byte[] imageBuffer;
    /** The size of a frame. */
    public final VideoDimensions dimensions;
    /** The orientation of a frame in degrees (must be multiple of 90). */
    public final int orientation;
    /** The time in nanoseconds at which this frame was captured. */
    public final long timestamp;

    public VideoFrame(byte[] imageBuffer,
                      VideoDimensions dimensions,
                      @VideoFrame.RotationAngle int orientation,
                      long timestamp) {
        this.imageBuffer = imageBuffer;
        this.dimensions = dimensions;
        this.orientation = orientation;
        this.timestamp = timestamp;
    }
}
