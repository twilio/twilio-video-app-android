package com.twilio.video;

import org.webrtc.VideoCapturer;

final class VideoCapturerObserverAdapter implements VideoCapturerObserver {
    private final VideoCapturer.CapturerObserver webRtcCapturerObserver;

    public VideoCapturerObserverAdapter(VideoCapturer.CapturerObserver webRtcCapturerObserver) {
        this.webRtcCapturerObserver = webRtcCapturerObserver;
    }

    @Override
    public void onCapturerStarted(boolean success) {
        webRtcCapturerObserver.onCapturerStarted(success);
    }

    @Override
    public void onFrameCaptured(CaptureFrame captureFrame) {
        webRtcCapturerObserver.onByteBufferFrameCaptured(captureFrame.getImageBuffer(),
                captureFrame.getDimensions().width,
                captureFrame.getDimensions().height,
                captureFrame.getOrientation(),
                captureFrame.getTimestamp());
    }
}
