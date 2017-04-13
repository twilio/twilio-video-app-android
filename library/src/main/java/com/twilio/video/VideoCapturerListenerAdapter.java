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
        /*
         * The imageBuffer field indicates if the frame is captured to a texture or not. Currently,
         * only the CameraCapturer captures to texture because we have not exposed capturing to
         * a texture to the VideoCapturer interface.
         */
        if (videoFrame.imageBuffer != null) {
            webRtcCapturerObserver.onByteBufferFrameCaptured(videoFrame.imageBuffer,
                    videoFrame.dimensions.width,
                    videoFrame.dimensions.height,
                    videoFrame.orientation.getValue(),
                    videoFrame.timestamp);
        } else {
            webRtcCapturerObserver.onTextureFrameCaptured(videoFrame.dimensions.width,
                    videoFrame.dimensions.height,
                    videoFrame.textureId,
                    videoFrame.transformMatrix,
                    videoFrame.orientation.getValue(),
                    videoFrame.timestamp);
        }
    }
}
