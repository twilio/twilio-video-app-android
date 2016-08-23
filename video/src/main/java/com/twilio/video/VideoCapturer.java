package com.twilio.video;

import java.util.List;

public interface VideoCapturer {
    List<CaptureFormat> getSupportedFormats();
    void startCapture(int width,
                      int height,
                      int framerate,
                      VideoCapturerObserver capturerObserver);
    void stopCapture();
}
