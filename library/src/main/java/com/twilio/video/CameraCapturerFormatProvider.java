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

import org.webrtc.Camera1Enumerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CameraCapturerFormatProvider {
    private static final Logger logger = Logger.getLogger(CameraCapturerFormatProvider.class);

    private final Map<CameraCapturer.CameraSource, List<VideoFormat>> supportedFormatsMap =
            new HashMap<>();
    private final Camera1Enumerator camera1Enumerator = new Camera1Enumerator(false);

    int getCameraId(CameraCapturer.CameraSource cameraSource) {
        int cameraId = -1;

        String[] deviceNames = camera1Enumerator.getDeviceNames();
        for(int i = 0; i < deviceNames.length; i++) {
            if ((camera1Enumerator.isFrontFacing(deviceNames[i]) &&
                    cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA) ||
                    (camera1Enumerator.isBackFacing(deviceNames[i]) && cameraSource ==
                            CameraCapturer.CameraSource.BACK_CAMERA)) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }

    String getDeviceName(int cameraId) {
        String[] deviceNames = camera1Enumerator.getDeviceNames();

        if (cameraId < 0 || cameraId >= deviceNames.length) {
            throw new IllegalArgumentException("cameraId not available on this device");
        }

        return deviceNames[cameraId];
    }

    List<VideoFormat> getSupportedFormats(CameraCapturer.CameraSource cameraSource) {
        List<VideoFormat> supportedFormats = supportedFormatsMap.get(cameraSource);

        if (supportedFormats == null) {
            supportedFormats = getSupportedFormats(getCameraId(cameraSource));
            supportedFormatsMap.put(cameraSource, supportedFormats);
        }

        return supportedFormats;
    }

    private List<VideoFormat> getSupportedFormats(int cameraId) {
        final List<VideoFormat> formatList = new ArrayList<>();
        final android.hardware.Camera.Parameters parameters;
        android.hardware.Camera camera = null;
        try {
            camera = android.hardware.Camera.open(cameraId);
            parameters = camera.getParameters();
        } catch (RuntimeException e) {
            logger.e(e.getMessage());
            return formatList;
        } finally {
            if (camera != null) {
                camera.release();
            }
        }

        int maxFps = 0;
        final List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
        if (listFpsRange != null) {
            // getSupportedPreviewFpsRange() returns a sorted list. Take the fps range
            // corresponding to the highest fps.
            final int[] range = listFpsRange.get(listFpsRange.size() - 1);
            maxFps = (range[android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX] + 999)
                    / 1000;
        }
        for (android.hardware.Camera.Size size : parameters.getSupportedPreviewSizes()) {
            VideoDimensions dimensions = new VideoDimensions(size.width, size.height);
            formatList.add(new VideoFormat(dimensions,
                    maxFps,
                    VideoPixelFormat.NV21));
        }

        return formatList;
    }
}
