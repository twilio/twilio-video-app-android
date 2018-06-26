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

package com.twilio.video.app.util;

import android.content.Context;
import android.util.Pair;
import com.twilio.video.Camera2Capturer;
import com.twilio.video.CameraCapturer;
import com.twilio.video.VideoCapturer;
import org.webrtc.Camera2Enumerator;
import timber.log.Timber;

/*
 * Simple wrapper class that uses Camera2Capturer with supported devices.
 */
public class CameraCapturerCompat {
  private CameraCapturer camera1Capturer;
  private Camera2Capturer camera2Capturer;
  private Pair<CameraCapturer.CameraSource, String> frontCameraPair;
  private Pair<CameraCapturer.CameraSource, String> backCameraPair;
  private final Camera2Capturer.Listener camera2Listener =
      new Camera2Capturer.Listener() {
        @Override
        public void onFirstFrameAvailable() {
          Timber.i("onFirstFrameAvailable");
        }

        @Override
        public void onCameraSwitched(String newCameraId) {
          Timber.i("onCameraSwitched: newCameraId = %s", newCameraId);
        }

        @Override
        public void onError(Camera2Capturer.Exception camera2CapturerException) {
          Timber.e(camera2CapturerException.getMessage());
        }
      };

  public CameraCapturerCompat(Context context, CameraCapturer.CameraSource cameraSource) {
    if (Camera2Capturer.isSupported(context)) {
      setCameraPairs(context);
      camera2Capturer = new Camera2Capturer(context, getCameraId(cameraSource), camera2Listener);
    } else {
      camera1Capturer = new CameraCapturer(context, cameraSource);
    }
  }

  public CameraCapturer.CameraSource getCameraSource() {
    if (usingCamera1()) {
      return camera1Capturer.getCameraSource();
    } else {
      return getCameraSource(camera2Capturer.getCameraId());
    }
  }

  public void switchCamera() {
    if (usingCamera1()) {
      camera1Capturer.switchCamera();
    } else {
      CameraCapturer.CameraSource cameraSource = getCameraSource(camera2Capturer.getCameraId());

      if (cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA) {
        camera2Capturer.switchCamera(backCameraPair.second);
      } else {
        camera2Capturer.switchCamera(frontCameraPair.second);
      }
    }
  }

  /*
   * This method is required because this class is not an implementation of VideoCapturer due to
   * a shortcoming in VideoCapturerDelegate where only instances of CameraCapturer,
   * Camera2Capturer, and ScreenCapturer are initialized correctly with a SurfaceTextureHelper.
   * Because capturing to a texture is not a part of the official public API we must expose
   * this method instead of writing a custom capturer so that camera capturers are properly
   * initialized.
   */
  public VideoCapturer getVideoCapturer() {
    if (usingCamera1()) {
      return camera1Capturer;
    } else {
      return camera2Capturer;
    }
  }

  private boolean usingCamera1() {
    return camera1Capturer != null;
  }

  private void setCameraPairs(Context context) {
    Camera2Enumerator camera2Enumerator = new Camera2Enumerator(context);
    for (String cameraId : camera2Enumerator.getDeviceNames()) {
      if (camera2Enumerator.isFrontFacing(cameraId)) {
        frontCameraPair = new Pair<>(CameraCapturer.CameraSource.FRONT_CAMERA, cameraId);
      }
      if (camera2Enumerator.isBackFacing(cameraId)) {
        backCameraPair = new Pair<>(CameraCapturer.CameraSource.BACK_CAMERA, cameraId);
      }
    }
  }

  private String getCameraId(CameraCapturer.CameraSource cameraSource) {
    if (frontCameraPair.first == cameraSource) {
      return frontCameraPair.second;
    } else {
      return backCameraPair.second;
    }
  }

  private CameraCapturer.CameraSource getCameraSource(String cameraId) {
    if (frontCameraPair.second.equals(cameraId)) {
      return frontCameraPair.first;
    } else {
      return backCameraPair.first;
    }
  }
}
