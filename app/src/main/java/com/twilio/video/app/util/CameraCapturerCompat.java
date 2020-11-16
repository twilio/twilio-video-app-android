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
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.twilio.video.Camera2Capturer;
import com.twilio.video.CameraCapturer;
import com.twilio.video.VideoCapturer;
import java.util.HashMap;
import java.util.Map;
import tvi.webrtc.Camera1Enumerator;
import tvi.webrtc.Camera2Enumerator;
import tvi.webrtc.CapturerObserver;
import tvi.webrtc.SurfaceTextureHelper;

/*
 * Simple wrapper class that uses Camera2Capturer with supported devices.
 */
public class CameraCapturerCompat implements VideoCapturer {
    private final CameraCapturer camera1Capturer;
    private final Camera2Capturer camera2Capturer;
    private final VideoCapturer activeCapturer;
    private final Map<Source, String> camera1IdMap = new HashMap<>();
    private final Map<String, Source> camera1SourceMap = new HashMap<>();
    private final Map<Source, String> camera2IdMap = new HashMap<>();
    private final Map<String, Source> camera2SourceMap = new HashMap<>();
    private CameraManager cameraManager;

    public enum Source {
        FRONT_CAMERA,
        BACK_CAMERA
    }

    public CameraCapturerCompat(Context context, Source cameraSource) {
        if (Camera2Capturer.isSupported(context) && isLollipopApiSupported()) {
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            setCamera2Maps(context);
            camera2Capturer = new Camera2Capturer(context, camera2IdMap.get(cameraSource));
            activeCapturer = camera2Capturer;
            camera1Capturer = null;
        } else {
            setCamera1Maps();
            camera1Capturer = new CameraCapturer(context, camera1IdMap.get(cameraSource));
            activeCapturer = camera1Capturer;
            camera2Capturer = null;
        }
    }

    private Source getCameraSource() {
        if (usingCamera1()) {
            return camera1SourceMap.get(camera1Capturer.getCameraId());
        } else {
            return camera2SourceMap.get(camera2Capturer.getCameraId());
        }
    }

    @Override
    public void initialize(
            SurfaceTextureHelper surfaceTextureHelper,
            Context context,
            CapturerObserver capturerObserver) {
        activeCapturer.initialize(surfaceTextureHelper, context, capturerObserver);
    }

    @Override
    public void startCapture(int width, int height, int framerate) {
        activeCapturer.startCapture(width, height, framerate);
    }

    @Override
    public void stopCapture() throws InterruptedException {
        activeCapturer.stopCapture();
    }

    @Override
    public boolean isScreencast() {
        return activeCapturer.isScreencast();
    }

    @Override
    public void dispose() {
        activeCapturer.dispose();
    }

    public void switchCamera() {
        Source cameraSource = getCameraSource();
        Map<Source, String> idMap = usingCamera1() ? camera1IdMap : camera2IdMap;
        String newCameraId =
                cameraSource == Source.FRONT_CAMERA
                        ? idMap.get(Source.BACK_CAMERA)
                        : idMap.get(Source.FRONT_CAMERA);

        if (usingCamera1()) {
            camera1Capturer.switchCamera(newCameraId);
        } else {
            camera2Capturer.switchCamera(newCameraId);
        }
    }

    private boolean usingCamera1() {
        return camera1Capturer != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setCamera2Maps(Context context) {
        Camera2Enumerator camera2Enumerator = new Camera2Enumerator(context);
        for (String cameraId : camera2Enumerator.getDeviceNames()) {
            if (isCameraIdSupported(cameraId)) {
                if (camera2Enumerator.isFrontFacing(cameraId)) {
                    camera2IdMap.put(Source.FRONT_CAMERA, cameraId);
                    camera2SourceMap.put(cameraId, Source.FRONT_CAMERA);
                }
                if (camera2Enumerator.isBackFacing(cameraId)) {
                    camera2IdMap.put(Source.BACK_CAMERA, cameraId);
                    camera2SourceMap.put(cameraId, Source.BACK_CAMERA);
                }
            }
        }
    }

    private void setCamera1Maps() {
        Camera1Enumerator camera1Enumerator = new Camera1Enumerator();
        for (String deviceName : camera1Enumerator.getDeviceNames()) {
            if (camera1Enumerator.isFrontFacing(deviceName)) {
                camera1IdMap.put(Source.FRONT_CAMERA, deviceName);
                camera1SourceMap.put(deviceName, Source.FRONT_CAMERA);
            }
            if (camera1Enumerator.isBackFacing(deviceName)) {
                camera1IdMap.put(Source.BACK_CAMERA, deviceName);
                camera1SourceMap.put(deviceName, Source.BACK_CAMERA);
            }
        }
    }

    private boolean isLollipopApiSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isCameraIdSupported(String cameraId) {
        boolean isMonoChromeSupported = false;
        boolean isPrivateImageFormatSupported = false;
        CameraCharacteristics cameraCharacteristics;
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
        } catch (Exception e) {
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

        if (streamMap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isPrivateImageFormatSupported = streamMap.isOutputSupportedFor(ImageFormat.PRIVATE);
        }

        /*
         * Read the color filter arrangements of the camera to filter out the ones that support
         * SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO or SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR.
         * Visit this link for details on supported values - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_COLOR_FILTER_ARRANGEMENT
         */
        Integer colorFilterArrangement =
                cameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && colorFilterArrangement != null) {
            isMonoChromeSupported =
                    colorFilterArrangement
                                    == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO
                            || colorFilterArrangement
                                    == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR;
        }
        return isPrivateImageFormatSupported && !isMonoChromeSupported;
    }
}
