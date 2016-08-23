package com.twilio.video;

public interface VideoCapturerObserver {
    void onCapturerStarted(boolean succes);
    void onByteBufferFrameCaptured(byte[] frame,
                                   int width,
                                   int height,
                                   int rotation,
                                   long timestamp);
    void onOutputFormatRequest(int width, int height, int framerate);
}
