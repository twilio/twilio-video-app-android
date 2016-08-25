package com.twilio.video;

public interface VideoCapturerObserver {
    void onCapturerStarted(boolean success);
    void onByteBufferFrameCaptured(CaptureFrame captureFrame);
}
