package com.twilio.rooms.helper;

import android.content.Context;

import com.twilio.rooms.CameraCapturer;
import com.twilio.rooms.CapturerErrorListener;
import com.twilio.rooms.CapturerException;

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
