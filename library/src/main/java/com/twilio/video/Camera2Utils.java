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

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;

@TargetApi(21)
class Camera2Utils {
  private static final Logger logger = Logger.getLogger(Camera2Utils.class);

  static boolean cameraIdSupported(@NonNull Context context, @NonNull String targetCameraId) {
    CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
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
