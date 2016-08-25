package com.twilio.video;

import android.graphics.ImageFormat;
import android.graphics.PixelFormat;

public enum CapturePixelFormat {
    NV21(ImageFormat.NV21),
    RGBA_8888(PixelFormat.RGBA_8888);

    private final int value;

    CapturePixelFormat(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
