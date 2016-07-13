package com.twilio.conversations.helper;

import android.content.Context;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;

public class CameraCapturerHelper {
    public static CameraCapturer createCameraCapturer(Context context,
                                                      CameraCapturer.CameraSource cameraSource) {
        return CameraCapturer.create(context, cameraSource,
                new CapturerErrorListener() {
                    @Override
                    public void onError(CapturerException e) {

                    }
                });
    }
}
