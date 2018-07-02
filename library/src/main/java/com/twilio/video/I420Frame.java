/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.support.annotation.Nullable;

import java.nio.ByteBuffer;

/**
 * A YUV frame in the I420 format. A frame can be represented as a {@link ByteBuffer} array of
 * Y, U, and V pixel data with an array of strides for each plane or as a texture.
 *
 * <p>
 *
 * When a frame is represented as a texture, {@link #textureId} will be set to a positive
 * non zero value with {@link #yuvPlanes} and {@link #yuvStrides} set to {@code null}. The YUV
 * data can be extracted from the texture using an instance of {@link org.webrtc.YuvConverter}
 * and the {@link #samplingMatrix}.
 *
 * <p>
 *
 * When a frame is represented as an array of {@link ByteBuffer}, {@link #textureId} will be
 * 0, {@link #yuvPlanes} contains the YUV pixel data, and {@link #yuvStrides} contains each
 * plane's stride.
 */
public class I420Frame {
    /** Width of frame */
    public final int width;
    /** Height of frame */
    public final int height;
    /**
     * Array of strides for each plane. This field is {@code null} when the frame is represented
     * as a texture.
     *
     * <p>
     *
     * yuvStrides[0] - Y stride. <br/>
     * yuvStrides[1] - U stride. <br/>
     * yuvStrides[2] - V stride. <br/>
     */
    @Nullable public final int[] yuvStrides;
    /**
     * Array of pixel data for each plane. This field is {@code null} when the frame is represented
     * as a texture.
     *
     * <p>
     *
     * yuvPlanes[0] - Y pixel data. <br/>
     * yuvPlanes[1] - U pixel data. <br/>
     * yuvPlanes[2] - V pixel data. <br/>
     */
    @Nullable public final ByteBuffer[] yuvPlanes;
    /** The degree that the frame must be rotated clockwise to be rendered correctly. */
    public int rotationDegree;
    /**
     * Id of the texture the frame is bound to. This field is zero when the frame is
     * represented as buffer.
     */
    public int textureId;
   /**
     * Matrix that transforms standard coordinates to their proper sampling locations in
     * the texture. This transform compensates for any properties of the video source that
     * cause it to appear different from a normalized texture. This matrix does not take
     * {@link #rotationDegree} into account.
     */
    public final float[] samplingMatrix;

    final org.webrtc.VideoRenderer.I420Frame webRtcI420Frame;
    long nativeFramePointer;

    I420Frame(org.webrtc.VideoRenderer.I420Frame webRtcI420Frame) {
        this.width = webRtcI420Frame.width;
        this.height = webRtcI420Frame.height;
        this.yuvStrides = webRtcI420Frame.yuvStrides;
        this.yuvPlanes = webRtcI420Frame.yuvPlanes;
        this.rotationDegree = webRtcI420Frame.rotationDegree;
        this.webRtcI420Frame = webRtcI420Frame;
        this.nativeFramePointer = webRtcI420Frame.nativeFramePointer;
        this.textureId = webRtcI420Frame.textureId;
        this.samplingMatrix = webRtcI420Frame.samplingMatrix;

        if (rotationDegree % 90 != 0) {
            throw new IllegalArgumentException("Rotation degree not multiple of 90: " +
                    rotationDegree);
        }
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

