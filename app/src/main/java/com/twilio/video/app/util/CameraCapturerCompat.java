/*
 * Copyright (C) 2019 Twilio, Inc.
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
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.twilio.video.Camera2Capturer;
import com.twilio.video.CameraCapturer;
import com.twilio.video.VideoCapturer;
import timber.log.Timber;
import tvi.webrtc.Camera2Enumerator;

/*
 * Simple wrapper class that uses Camera2Capturer with supported devices.
 */
public class CameraCapturerCompat {
    private CameraCapturer camera1Capturer;
    private Camera2Capturer camera2Capturer;
    private Pair<CameraCapturer.CameraSource, String> frontCameraPair;
    private Pair<CameraCapturer.CameraSource, String> backCameraPair;
    private CameraManager cameraManager;

    public CameraCapturerCompat(Context context, CameraCapturer.CameraSource cameraSource) {
        if (Camera2Capturer.isSupported(context) && isLollipopApiSupported()) {
            cameraManager =
                    (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            setCameraPairs(context);
            Camera2Capturer.Listener camera2Listener =
                    new Camera2Capturer.Listener() {
                        @Override
                        public void onFirstFrameAvailable() {
                            Timber.i("onFirstFrameAvailable");
                        }

                        @Override
                        public void onCameraSwitched(@NonNull String newCameraId) {
                            Timber.i("onCameraSwitched: newCameraId = %s", newCameraId);
                        }

                        @Override
                        public void onError(
                                @NonNull Camera2Capturer.Exception camera2CapturerException) {
                            Timber.e(camera2CapturerException);
                        }
                    };
            camera2Capturer =
                    new Camera2Capturer(context, getCameraId(cameraSource), camera2Listener);
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
            CameraCapturer.CameraSource cameraSource =
                    getCameraSource(camera2Capturer.getCameraId());

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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setCameraPairs(Context context) {
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(context);
        for (String cameraId : camera2Enumerator.getDeviceNames()) {
            if (isCameraIdSupported(cameraId)) {
                if (camera2Enumerator.isFrontFacing(cameraId)) {
                    frontCameraPair = new Pair<>(CameraCapturer.CameraSource.FRONT_CAMERA, cameraId);
                }
                if (camera2Enumerator.isBackFacing(cameraId)) {
                    backCameraPair = new Pair<>(CameraCapturer.CameraSource.BACK_CAMERA, cameraId);
                }
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

    private boolean isLollipopApiSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isCameraIdSupported(String cameraId) {
        boolean isMonoChromeSupported = false;
        boolean isPrivateImageFormatSupported;
        CameraCharacteristics cameraCharacteristics;
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
        /*
         * This is a temporary work around for a RuntimeException that occurs on devices which contain cameras
         * that do not support ImageFormat.PRIVATE output formats. A long term fix is currently in development.
         * https://github.com/twilio/video-quickstart-android/issues/431
         */
        final StreamConfigurationMap streamMap =
                cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        isPrivateImageFormatSupported = streamMap.isOutputSupportedFor(ImageFormat.PRIVATE);

        /*
         * Read the color filter arrangements of the camera to filter out the ones that support
         * SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO or SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR.
         * Visit this link for details on supported values - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_COLOR_FILTER_ARRANGEMENT
         */
        final int colorFilterArrangement = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isMonoChromeSupported = (colorFilterArrangement == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO
                    || colorFilterArrangement == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR) ? true : false;
        }
        return isPrivateImageFormatSupported && !isMonoChromeSupported;
    }
}
