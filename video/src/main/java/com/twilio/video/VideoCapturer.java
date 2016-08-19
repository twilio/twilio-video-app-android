package com.twilio.video;

import android.content.Context;

import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.SurfaceTextureHelper;

import java.util.List;

public abstract class VideoCapturer implements org.webrtc.VideoCapturer {
    @Override
    public List<CameraEnumerationAndroid.CaptureFormat> getSupportedFormats() {
        return null;
    }

    @Override
    public void startCapture(int width,
                             int height,
                             int framerate,
                             SurfaceTextureHelper surfaceTextureHelper,
                             Context context,
                             CapturerObserver capturerObserver) {
    }

    abstract void startCapture();

    @Override
    public void stopCapture() throws InterruptedException {

    }

    @Override
    public void dispose() {

    }
}
