package com.twilio.video;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Represents a video frame provided by a {@link CameraCapturer}.
 */
public class VideoFrame {

    public enum RotationAngle {
        ROTATION_0(0),
        ROTATION_90(90),
        ROTATION_180(180),
        ROTATION_270(270);

        private final int rotation;
        private RotationAngle(int rotation) {
            this.rotation = rotation;
        }

        public int getValue() {
            return this.rotation;
        }

        public static RotationAngle fromInt(int rotation) {
            if (rotation == 0) {
                return ROTATION_0;
            } else if (rotation == 90) {
                return ROTATION_90;
            } else if (rotation == 180) {
                return ROTATION_180;
            } else if (rotation == 270) {
                return ROTATION_270;
            } else {
                throw new IllegalArgumentException(
                    "Orientation value must be 0, 90, 180 or 270: " + rotation);
            }
        }
    }

    /** The bytes of a frame. */
    public final byte[] imageBuffer;
    /** The size of a frame. */
    public final VideoDimensions dimensions;
    /** The orientation of a frame in degrees (must be multiple of 90). */
    public final RotationAngle orientation;
    /** The time in nanoseconds at which this frame was captured. */
    public final long timestamp;

    public VideoFrame(byte[] imageBuffer,
                      VideoDimensions dimensions,
                      RotationAngle orientation,
                      long timestamp) {
        this.imageBuffer = imageBuffer;
        this.dimensions = dimensions;
        this.orientation = orientation;
        this.timestamp = timestamp;
    }
}
