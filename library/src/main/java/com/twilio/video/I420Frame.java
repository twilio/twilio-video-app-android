package com.twilio.video;

import java.nio.ByteBuffer;

/**
 * A YUV frame in the I420 format.
 */
public class I420Frame {
    /** Width of frame */
    public final int width;
    /** Height of frame */
    public final int height;
    /**
     * Array of strides for each plane.
     *
     * yuvStrides[0] - Y stride.
     * yuvStrides[1] - U stride.
     * yuvStrides[2] - V stride.
     */
    public final int[] yuvStrides;
    /**
     * Array of pixel data for each plane.
     *
     * yuvPlanes[0] - Y pixel data.
     * yuvPlanes[1] - U pixel data.
     * yuvPlanes[2] - V pixel data.
     */
    public final ByteBuffer[] yuvPlanes;
    /** The degree that the frame must be rotated clockwise to be rendered correctly. */
    public int rotationDegree;
    long nativeFramePointer;
    /*
     * Matrix that transforms standard coordinates to their proper sampling locations in
     * the texture. This transform compensates for any properties of the video source that
     * cause it to appear different from a normalized texture. This matrix does not take
     * |rotationDegree| into account.
     */
    private final float[] samplingMatrix;

    I420Frame(int width,
              int height,
              int rotationDegree,
              int[] yuvStrides,
              ByteBuffer[] yuvPlanes,
              long nativeFramePointer) {
        this.width = width;
        this.height = height;
        this.yuvStrides = yuvStrides;
        this.yuvPlanes = yuvPlanes;
        this.rotationDegree = rotationDegree;
        this.nativeFramePointer = nativeFramePointer;
        if (rotationDegree % 90 != 0) {
            throw new IllegalArgumentException("Rotation degree not multiple of 90: " +
                    rotationDegree);
        }
        /*
         * The convention in WebRTC is that the first element in a ByteBuffer corresponds to the
         * top-left corner of the image, but in glTexImage2D() the first element corresponds to the
         * bottom-left corner. This discrepancy is corrected by setting a vertical flip as sampling
         * matrix.
         */
        samplingMatrix = new float[] {
                1,  0, 0, 0,
                0, -1, 0, 0,
                0,  0, 1, 0,
                0,  1, 0, 1};
    }

    /**
     * Returns the width of the frame based on the current {@link #rotationDegree}.
     */
    public int rotatedWidth() {
        return (rotationDegree % 180 == 0) ? width : height;
    }

    /**
     * Returns the height of the frame based on the current {@link #rotationDegree}.
     */
    public int rotatedHeight() {
        return (rotationDegree % 180 == 0) ? height : width;
    }

    /**
     * Called when a {@link VideoRenderer} has completed rendering a frame. This must be invoked
     * for each frame to ensure that resources are not leaked.
     */
    public synchronized void release() {
        if (nativeFramePointer != 0) {
            nativeRelease(nativeFramePointer);
            nativeFramePointer = 0;
        }
    }

    @Override
    public String toString() {
        return width + "x" + height + ":" + yuvStrides[0] + ":" + yuvStrides[1] +
                ":" + yuvStrides[2];
    }

    private native void nativeRelease(long nativeFramePointer);
}

