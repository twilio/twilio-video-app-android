package com.twilio.conversations;

public class VideoDimensions {

    public final int width;
    public final int height;

    public VideoDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return String.valueOf(width) + "x" + String.valueOf(height);
    }
}
