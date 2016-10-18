package com.twilio.video;

import android.content.Context;

import org.webrtc.*;

import java.util.ArrayList;
import java.util.List;

final class VideoCapturerDelegate implements org.webrtc.VideoCapturer {
    private final VideoCapturer videoCapturer;
    private VideoPixelFormat videoPixelFormat;

    VideoCapturerDelegate(VideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
    }

    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        return convertToWebRtcFormats(videoCapturer.getSupportedFormats());
    }

    @Override
    public void startCapture(int width,
                             int height,
                             int framerate,
                             SurfaceTextureHelper surfaceTextureHelper,
                             Context context,
                             CapturerObserver capturerObserver) {
        // FIXME: ugh this is still cheating..need to figure out a way to pass this better
        if (videoCapturer instanceof CameraCapturer) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.setSurfaceTextureHelper(surfaceTextureHelper);
        }
        VideoDimensions dimensions = new VideoDimensions(width, height);
        VideoFormat captureFormat = new VideoFormat(dimensions, framerate, videoPixelFormat);
        VideoCapturerListenerAdapter listenerAdapter =
                new VideoCapturerListenerAdapter(capturerObserver);

        videoCapturer.startCapture(captureFormat, listenerAdapter);
    }

    @Override
    public void stopCapture() throws InterruptedException {
        videoCapturer.stopCapture();
    }

    @Override
    public void dispose() {
        // Currently this is not part of our capturer api so we can just ignore
    }

    /*
     * Called from JNI layer prior to capturing so that we can provide the pixel format
     * when informing the current capturer to start
     */
    private void setVideoPixelFormat(VideoPixelFormat videoPixelFormat) {
        this.videoPixelFormat = videoPixelFormat;
    }

    private List<CameraEnumerationAndroid.CaptureFormat> convertToWebRtcFormats(List<VideoFormat> videoFormats) {
        if (videoFormats != null) {
            List<CameraEnumerationAndroid.CaptureFormat> webRtcCaptureFormats =
                    new ArrayList<>(videoFormats.size());

            for (int i = 0; i < videoFormats.size() ; i++) {
                VideoFormat videoFormat = videoFormats.get(i);
                CameraEnumerationAndroid.CaptureFormat webRtcCaptureFormat =
                        /*
                         * WebRTC wants min and max framerates in their format but only uses
                         * max framerate in JNI layer. We will just set min and max to the same in
                         * this conversion
                         */
                        new CameraEnumerationAndroid.CaptureFormat(videoFormat.dimensions.width,
                                videoFormat.dimensions.height,
                                videoFormat.framerate,
                                videoFormat.framerate,
                                videoFormat.pixelFormat.getValue());

                webRtcCaptureFormats.add(i, webRtcCaptureFormat);
            }

            return webRtcCaptureFormats;
        }

        return new ArrayList<>();
    }
}
