package com.twilio.video;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;

public enum VideoPixelFormat {
    NV21(ImageFormat.NV21),
    RGBA_8888(PixelFormat.RGBA_8888);

    private final int value;

    VideoPixelFormat(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
