package com.twilio.video.helper;

import android.content.Context;

import com.twilio.video.CameraCapturer;
import com.twilio.video.CapturerErrorListener;
import com.twilio.video.CapturerException;

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
