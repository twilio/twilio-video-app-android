package com.twilio.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;

@TargetApi(21)
class Camera2Utils {
    private static final Logger logger = Logger.getLogger(Camera2Utils.class);

    static boolean cameraIdSupported(@NonNull Context context, @NonNull String targetCameraId) {
        CameraManager cameraManager = (CameraManager)
                context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                if (targetCameraId.equals(cameraId)) {
                    return true;
                }
            }
        } catch (CameraAccessException e) {
            logger.e(e.getMessage());
        }

        return false;
    }
}
