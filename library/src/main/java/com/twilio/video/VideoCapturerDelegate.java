/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.content.Context;
import java.util.List;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoFrame;

final class VideoCapturerDelegate implements org.webrtc.VideoCapturer {
    private final VideoCapturer videoCapturer;
    private VideoPixelFormat videoPixelFormat;
    private VideoCapturer.Listener listenerAdapter;

    /*
     * Created from JNI.
     */
    @SuppressWarnings("unused")
    VideoCapturerDelegate(VideoCapturer videoCapturer) {
        this.videoCapturer = videoCapturer;
    }

    /*
     * Invoked from JNI
     */
    @SuppressWarnings("unused")
    public List<VideoFormat> getSupportedFormats() {
        return videoCapturer.getSupportedFormats();
    }

    @Override
    public void initialize(
            SurfaceTextureHelper surfaceTextureHelper,
            Context context,
            CapturerObserver capturerObserver) {
        this.listenerAdapter = new VideoCapturerListenerAdapter(capturerObserver);
        // FIXME: ugh this is still cheating..need to figure out a way to pass this better
        if (videoCapturer instanceof CameraCapturer) {
            CameraCapturer cameraCapturer = (CameraCapturer) videoCapturer;

            cameraCapturer.setSurfaceTextureHelper(surfaceTextureHelper);
        } else if (videoCapturer instanceof ScreenCapturer) {
            ScreenCapturer screenCapturer = (ScreenCapturer) videoCapturer;

            screenCapturer.setSurfaceTextureHelper(surfaceTextureHelper);
        } else if (videoCapturer instanceof Camera2Capturer) {
            Camera2Capturer camera2Capturer = (Camera2Capturer) videoCapturer;

            camera2Capturer.setSurfaceTextureHelper(surfaceTextureHelper);
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
    @SuppressWarnings("unused")
    private void setVideoPixelFormat(VideoPixelFormat videoPixelFormat) {
        this.videoPixelFormat = videoPixelFormat;
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
        public void onByteBufferFrameCaptured(
                byte[] data, int width, int height, int rotation, long timeStamp) {
            nativeOnByteBufferFrameCaptured(
                    nativeCapturer, data, data.length, width, height, rotation, timeStamp);
        }

        @Override
        public void onTextureFrameCaptured(
                int width,
                int height,
                int oesTextureId,
                float[] transformMatrix,
                int rotation,
                long timestamp) {
            nativeOnTextureFrameCaptured(
                    nativeCapturer,
                    width,
                    height,
                    oesTextureId,
                    transformMatrix,
                    rotation,
                    timestamp);
        }

        @Override
        public void onFrameCaptured(VideoFrame videoFrame) {
            VideoFrame.Buffer buffer = videoFrame.getBuffer();
            nativeOnFrameCaptured(
                    nativeCapturer,
                    buffer.getWidth(),
                    buffer.getHeight(),
                    videoFrame.getTimestampNs(),
                    videoFrame.getRotation(),
                    buffer);
        }

        private native void nativeCapturerStarted(long nativeCapturer, boolean success);

        private native void nativeOnByteBufferFrameCaptured(
                long nativeCapturer,
                byte[] data,
                int length,
                int width,
                int height,
                int rotation,
                long timeStamp);

        private native void nativeOnTextureFrameCaptured(
                long nativeCapturer,
                int width,
                int height,
                int oesTextureId,
                float[] transformMatrix,
                int rotation,
                long timestamp);

        private native void nativeOnFrameCaptured(
                long nativeCapturer,
                int width,
                int height,
                long timeStamp,
                int rotation,
                org.webrtc.VideoFrame.Buffer webRtcVideoFrameBuffer);
    }
}
