package com.twilio.conversations;

import java.nio.ByteBuffer;

/**
 * A YUV frame in the I420 format
 */
public class I420Frame {
    public final int width;
    public final int height;
    public final int[] yuvStrides;
    public ByteBuffer[] yuvPlanes;
    public final boolean yuvFrame;
    // Matrix that transforms standard coordinates to their proper sampling locations in
    // the texture. This transform compensates for any properties of the video source that
    // cause it to appear different from a normalized texture. This matrix does not take
    // |rotationDegree| into account.
    public final float[] samplingMatrix;
    public int textureId;
    // Frame pointer in C++.
    long nativeFramePointer;

    // rotationDegree is the degree that the frame must be rotated clockwisely
    // to be rendered correctly.
    public int rotationDegree;

    /**
     * Construct a frame of the given dimensions with the specified planar data.
     */
    public I420Frame(int width, int height, int rotationDegree,
                     int[] yuvStrides, ByteBuffer[] yuvPlanes, long nativeFramePointer) {
        this.width = width;
        this.height = height;
        this.yuvStrides = yuvStrides;
        this.yuvPlanes = yuvPlanes;
        this.yuvFrame = true;
        this.rotationDegree = rotationDegree;
        this.nativeFramePointer = nativeFramePointer;
        if (rotationDegree % 90 != 0) {
            throw new IllegalArgumentException("Rotation degree not multiple of 90: " + rotationDegree);
        }
        // The convention in WebRTC is that the first element in a ByteBuffer corresponds to the
        // top-left corner of the image, but in glTexImage2D() the first element corresponds to the
        // bottom-left corner. This discrepancy is corrected by setting a vertical flip as sampling
        // matrix.
        samplingMatrix = new float[] {
                1,  0, 0, 0,
                0, -1, 0, 0,
                0,  0, 1, 0,
                0,  1, 0, 1};
    }

    /**
     * Construct a texture frame of the given dimensions with data in SurfaceTexture
     */
    public I420Frame(int width, int height, int rotationDegree,
                     int textureId, float[] samplingMatrix, long nativeFramePointer) {
        this.width = width;
        this.height = height;
        this.yuvStrides = null;
        this.yuvPlanes = null;
        this.samplingMatrix = samplingMatrix;
        this.textureId = textureId;
        this.yuvFrame = false;
        this.rotationDegree = rotationDegree;
        this.nativeFramePointer = nativeFramePointer;
        if (rotationDegree % 90 != 0) {
            throw new IllegalArgumentException("Rotation degree not multiple of 90: " + rotationDegree);
        }
    }

    public int rotatedWidth() {
        return (rotationDegree % 180 == 0) ? width : height;
    }

    public int rotatedHeight() {
        return (rotationDegree % 180 == 0) ? height : width;
    }

    @Override
    public String toString() {
        return width + "x" + height + ":" + yuvStrides[0] + ":" + yuvStrides[1] +
                ":" + yuvStrides[2];
    }
}

