package com.twilio.video;

final class VideoCapturerListenerAdapter implements VideoCapturer.Listener {
    private final org.webrtc.VideoCapturer.CapturerObserver webRtcCapturerObserver;

    public VideoCapturerListenerAdapter(org.webrtc.VideoCapturer.CapturerObserver webRtcCapturerObserver) {
        this.webRtcCapturerObserver = webRtcCapturerObserver;
    }

    @Override
    public void onCapturerStarted(boolean success) {
        webRtcCapturerObserver.onCapturerStarted(success);
    }

    @Override
    public void onFrameCaptured(VideoFrame videoFrame) {
        webRtcCapturerObserver.onByteBufferFrameCaptured(videoFrame.imageBuffer,
                videoFrame.dimensions.width,
                videoFrame.dimensions.height,
                videoFrame.orientation,
                videoFrame.timestamp);
    }
}
