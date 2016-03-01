package com.twilio.conversations.impl;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.LocalVideoTrack;

public class LocalVideoTrackImpl extends VideoTrackImpl implements LocalVideoTrack  {
    private CameraCapturer cameraCapturer;

    public LocalVideoTrackImpl(CameraCapturer cameraCapturer) {
        super();
        this.cameraCapturer = cameraCapturer;
    }

    @Override
    public CameraCapturer getCameraCapturer() {
        return cameraCapturer;
    }

    @Override
    public boolean enable(boolean enabled) {
        if (super.enable(enabled)) {
            if(enabled) {
                ((CameraCapturerImpl)cameraCapturer).resume();
            } else {
                ((CameraCapturerImpl)cameraCapturer).pause();
            }
            return true;
        }
        return false;
    }

    void removeCameraCapturer() {
        ((CameraCapturerImpl)cameraCapturer).resetNativeVideoCapturer();
        cameraCapturer = null;
    }
}
