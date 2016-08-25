package com.twilio.video;

import java.util.List;

public interface VideoCapturer {
    List<VideoFormat> getSupportedFormats();
    void startCapture(int width,
                      int height,
                      int framerate,
                      Listener capturerListener);
    void stopCapture();

    interface Listener {
        void onCapturerStarted(boolean success);
        void onFrameCaptured(VideoFrame videoFrame);
    }
}
