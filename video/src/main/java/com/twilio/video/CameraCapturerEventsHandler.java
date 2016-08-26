package com.twilio.video;

import org.webrtc.VideoCapturerAndroid;

class CameraCapturerEventsHandler implements VideoCapturerAndroid.CameraEventsHandler {
    private final CapturerErrorListener listener;

    public CameraCapturerEventsHandler(CapturerErrorListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCameraError(String errorMsg) {
        if(listener != null) {
            listener.onError(new CapturerException(CapturerException.ExceptionDomain.CAMERA,
                    errorMsg));
        }
    }

    @Override
    public void onCameraFreezed(String s) {

    }

    @Override
    public void onCameraOpening(int i) {

    }

    @Override
    public void onFirstFrameAvailable() {

    }

    @Override
    public void onCameraClosed() {

    }
}
