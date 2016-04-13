package com.twilio.conversations.helper;

import android.content.Context;
import android.widget.RelativeLayout;

import com.twilio.conversations.CameraCapturer;
import com.twilio.conversations.CameraCapturerFactory;
import com.twilio.conversations.CapturerErrorListener;
import com.twilio.conversations.CapturerException;

public class CameraCapturerHelper {

    public static CameraCapturer createCameraCapturer(Context context, CameraCapturer.CameraSource cameraSource) {
        return CameraCapturerFactory.createCameraCapturer(
                context,
                cameraSource,
                new CapturerErrorListener() {
                    @Override
                    public void onError(CapturerException e) {

                    }
                });

    }
}
