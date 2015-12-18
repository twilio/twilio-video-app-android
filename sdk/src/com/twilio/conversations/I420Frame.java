package com.twilio.conversations;

import java.nio.ByteBuffer;

  /**
   * A YUV frame in the I420 format
   *
   */
  public class I420Frame {
    public final int width;
    public final int height;
    public final int[] yuvStrides;
    public final ByteBuffer[] yuvPlanes;
    public final boolean yuvFrame;
    public Object textureObject;
    public int textureId;

    // rotationDegree is the degree that the frame must be rotated clockwisely
    // to be rendered correctly.
    public int rotationDegree;

    /**
     * Construct a frame of the given dimensions with the specified planar
     * data.  If |yuvPlanes| is null, new planes of the appropriate sizes are
     * allocated.
     */
    public I420Frame(
        int width, int height, int rotationDegree,
        int[] yuvStrides, ByteBuffer[] yuvPlanes) {
      this.width = width;
      this.height = height;
      this.yuvStrides = yuvStrides;
      if (yuvPlanes == null) {
        yuvPlanes = new ByteBuffer[3];
        yuvPlanes[0] = ByteBuffer.allocateDirect(yuvStrides[0] * height);
        yuvPlanes[1] = ByteBuffer.allocateDirect(yuvStrides[1] * height / 2);
        yuvPlanes[2] = ByteBuffer.allocateDirect(yuvStrides[2] * height / 2);
      }
      this.yuvPlanes = yuvPlanes;
      this.yuvFrame = true;
      this.rotationDegree = rotationDegree;
    }

    /**
     * Construct a texture frame of the given dimensions with data in SurfaceTexture
     */
    public I420Frame(
        int width, int height, int rotationDegree,
        Object textureObject, int textureId) {
      this.width = width;
      this.height = height;
      this.yuvStrides = null;
      this.yuvPlanes = null;
      this.textureObject = textureObject;
      this.textureId = textureId;
      this.yuvFrame = false;
      this.rotationDegree = rotationDegree;
    }

    /**
     * Copy the planes out of |source| into |this| and return |this|.  Calling
     * this with mismatched frame dimensions or frame type is a programming
     * error and will likely crash.
     */
    public I420Frame copyFrom(I420Frame source) {

      if (source.yuvFrame && yuvFrame) {
        if (width != source.width || height != source.height) {
          throw new RuntimeException("Mismatched dimensions!  Source: " +
              source.toString() + ", destination: " + toString());
        }
        copyPlane(source.yuvPlanes[0], yuvPlanes[0]);
        copyPlane(source.yuvPlanes[1], yuvPlanes[1]);
        copyPlane(source.yuvPlanes[2], yuvPlanes[2]);
        return this;
      } else if (!source.yuvFrame && !yuvFrame) {
        textureObject = source.textureObject;
        textureId = source.textureId;
        rotationDegree = source.rotationDegree;
        return this;
      } else {
        throw new RuntimeException("Mismatched frame types!  Source: " +
            source.toString() + ", destination: " + toString());
      }
    }

    // Copy the bytes out of |src| and into |dst|, ignoring and overwriting
    // positon & limit in both buffers.
    private void copyPlane(ByteBuffer src, ByteBuffer dst) {
      src.position(0).limit(src.capacity());
      dst.put(src);
      dst.position(0).limit(dst.capacity());
    }

    public I420Frame copyFrom(byte[] yuvData, int rotationDegree) {
      if (yuvData.length < width * height * 3 / 2) {
        throw new RuntimeException("Wrong arrays size: " + yuvData.length);
      }
      if (!yuvFrame) {
        throw new RuntimeException("Can not feed yuv data to texture frame");
      }
      int planeSize = width * height;
      ByteBuffer[] planes = new ByteBuffer[3];
      planes[0] = ByteBuffer.wrap(yuvData, 0, planeSize);
      planes[1] = ByteBuffer.wrap(yuvData, planeSize, planeSize / 4);
      planes[2] = ByteBuffer.wrap(yuvData, planeSize + planeSize / 4,
          planeSize / 4);
      for (int i = 0; i < 3; i++) {
        yuvPlanes[i].position(0);
        yuvPlanes[i].put(planes[i]);
        yuvPlanes[i].position(0);
        yuvPlanes[i].limit(yuvPlanes[i].capacity());
      }
      this.rotationDegree = rotationDegree;
      return this;
    }

    @Override
    public String toString() {
      return width + "x" + height + ":" + yuvStrides[0] + ":" + yuvStrides[1] +
          ":" + yuvStrides[2];
    }
  }

