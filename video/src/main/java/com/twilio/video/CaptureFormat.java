package com.twilio.video;

public class CaptureFormat {
    public final int width;
    public final int height;
    public final int maxFramerate;
    public final int minFramerate;
    public final int imageFormat;

    public CaptureFormat(int width,
                         int height,
                         int minFramerate,
                         int maxFramerate,
                         int imageFormat) {
        this.width = width;
        this.height = height;
        this.minFramerate = minFramerate;
        this.maxFramerate = maxFramerate;
        this.imageFormat = imageFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CaptureFormat that = (CaptureFormat) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        if (maxFramerate != that.maxFramerate) return false;
        if (minFramerate != that.minFramerate) return false;
        return imageFormat == that.imageFormat;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        result = 31 * result + maxFramerate;
        result = 31 * result + minFramerate;
        result = 31 * result + imageFormat;
        return result;
    }

    @Override
    public String toString() {
        return "CaptureFormat{" +
                "width=" + width +
                ", height=" + height +
                ", maxFramerate=" + maxFramerate +
                ", minFramerate=" + minFramerate +
                ", imageFormat=" + imageFormat +
                '}';
    }
}
