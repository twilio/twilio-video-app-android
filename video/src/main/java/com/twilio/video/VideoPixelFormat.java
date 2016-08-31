package com.twilio.video;

public enum VideoPixelFormat {
    NV21,
    RGBA_8888;

    private final int value;

    VideoPixelFormat() {
        this.value = nativeGetValue(name());
    }

    int getValue() {
        return value;
    }

    private native int nativeGetValue(String name);
}
