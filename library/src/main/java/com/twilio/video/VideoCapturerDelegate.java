package com.twilio.video;

import android.content.Context;

import org.webrtc.*;

import java.util.ArrayList;
import java.util.List;

final class VideoCapturerDelegate implements org.webrtc.VideoCapturer {
    private final VideoCapturer videoCapturer;
    private VideoPixelFormat videoPixelFormat;
    private VideoCapturer.Listener listenerAdapter;

    VideoCapturerDelegate(VideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
    }

    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        return convertToWebRtcFormats(videoCapturer.getSupportedFormats());
    }

    @Override
    public void initialize(SurfaceTextureHelper surfaceTextureHelper,
                           Context context,
                           CapturerObserver capturerObserver) {
        this.listenerAdapter = new VideoCapturerListenerAdapter(capturerObserver);
        // FIXME: ugh this is still cheating..need to figure out a way to pass this better
        if (videoCapturer instanceof CameraCapturer) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.setSurfaceTextureHelper(surfaceTextureHelper);
        }
    }

    @Override
    public void startCapture(int width, int height, int framerate) {
        VideoDimensions dimensions = new VideoDimensions(width, height);
        VideoFormat captureFormat = new VideoFormat(dimensions, framerate, videoPixelFormat);
        videoCapturer.startCapture(captureFormat, listenerAdapter);
    }

    @Override
    public void stopCapture() throws InterruptedException {
        videoCapturer.stopCapture();
    }

    @Override
    public void changeCaptureFormat(int width, int height, int framerate) {
        // Currently this is not part of our capturer api so we can just ignore
    }

    @Override
    public void dispose() {
        // Currently this is not part of our capturer api so we can just ignore
    }

    @Override
    public boolean isScreencast() {
        return videoCapturer.isScreencast();
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

            for (int i = 0; i < videoFormats.size(); i++) {
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


    /*
     * An implementation of CapturerObserver that forwards all calls from Java to the C layer.
     */
    static class NativeObserver implements org.webrtc.VideoCapturer.CapturerObserver {
        private final long nativeCapturer;

        public NativeObserver(long nativeCapturer) {
            this.nativeCapturer = nativeCapturer;
        }

        @Override
        public void onCapturerStarted(boolean success) {
            nativeCapturerStarted(nativeCapturer, success);
        }

        @Override
        public void onCapturerStopped() {
            // Not currently required in our capturer API
        }

        @Override
        public void onByteBufferFrameCaptured(byte[] data,
                                              int width,
                                              int height,
                                              int rotation,
                                              long timeStamp) {
            nativeOnByteBufferFrameCaptured(nativeCapturer,
                    data,
                    data.length,
                    width,
                    height,
                    rotation,
                    timeStamp);
        }

        @Override
        public void onTextureFrameCaptured(int width,
                                           int height,
                                           int oesTextureId,
                                           float[] transformMatrix,
                                           int rotation,
                                           long timestamp) {
            nativeOnTextureFrameCaptured(nativeCapturer,
                    width,
                    height,
                    oesTextureId,
                    transformMatrix,
                    rotation,
                    timestamp);
        }

        private native void nativeCapturerStarted(long nativeCapturer,
                                                  boolean success);

        private native void nativeOnByteBufferFrameCaptured(long nativeCapturer,
                                                            byte[] data,
                                                            int length,
                                                            int width,
                                                            int height,
                                                            int rotation,
                                                            long timeStamp);

        private native void nativeOnTextureFrameCaptured(long nativeCapturer,
                                                         int width,
                                                         int height,
                                                         int oesTextureId,
                                                         float[] transformMatrix,
                                                         int rotation,
                                                         long timestamp);
    }
}
