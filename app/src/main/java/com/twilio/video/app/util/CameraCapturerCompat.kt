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
package com.twilio.video.app.util

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.twilio.video.Camera2Capturer
import com.twilio.video.CameraCapturer
import com.twilio.video.VideoCapturer
import timber.log.Timber
import tvi.webrtc.Camera1Enumerator
import tvi.webrtc.Camera2Enumerator
import tvi.webrtc.CameraEnumerator

/*
 * Simple wrapper class that uses Camera2Capturer with supported devices.
 */
class CameraCapturerCompat {
    private var cameraManager: CameraManager? = null
    private val camera2Listener: Camera2Capturer.Listener = object : Camera2Capturer.Listener {
        override fun onFirstFrameAvailable() {
            Timber.i("onFirstFrameAvailable")
        }

        override fun onCameraSwitched(newCameraId: String) {
            Timber.i("onCameraSwitched: newCameraId = %s", newCameraId)
        }

        override fun onError(
            camera2CapturerException: Camera2Capturer.Exception
        ) {
            Timber.e(camera2CapturerException)
        }
    }
//    val cameraSource: CameraCapturer.CameraSource
//        get() = if (usingCamera1()) {
//            camera1Capturer.getCameraSource()
//        } else {
//            getCameraSource(camera2Capturer!!.cameraId)
//        }

    fun switchCamera() {
//        if (usingCamera1()) {
//            camera1Capturer!!.switchCamera()
//        } else {
//            val cameraSource: CameraCapturer.CameraSource = getCameraSource(camera2Capturer!!.cameraId)
//            if (cameraSource === CameraCapturer.CameraSource.FRONT_CAMERA) {
//                camera2Capturer!!.switchCamera(backCameraPair!!.second)
//            } else {
//                camera2Capturer!!.switchCamera(frontCameraPair!!.second)
//            }
//        }
    }

    /*
     * This property is required because this class is not an implementation of VideoCapturer due to
     * a shortcoming in VideoCapturerDelegate where only instances of CameraCapturer,
     * Camera2Capturer, and ScreenCapturer are initialized correctly with a SurfaceTextureHelper.
     * Because capturing to a texture is not a part of the official public API we must expose
     * this method instead of writing a custom capturer so that camera capturers are properly
     * initialized.
     */
    lateinit var videoCapturer: VideoCapturer

    companion object {
        fun newInstance(context: Context): CameraCapturerCompat? {
            return if (Camera2Capturer.isSupported(context)) {
                val cameraId = Camera2Enumerator(context).run { getFrontOrBackCameraId() }
                cameraId?.let {
                    CameraCapturerCompat().apply {
                        videoCapturer = Camera2Capturer(context, it, camera2Listener)
                    }
                }
            } else {
                val cameraId = Camera1Enumerator().run { getFrontOrBackCameraId() }
                cameraId?.let {
                    CameraCapturerCompat().apply {
                        videoCapturer = CameraCapturer(context, it)
                    }
                }
            }
//            if (Camera2Capturer.isSupported(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//                val camera2Listener: Camera2Capturer.Listener = object : Camera2Capturer.Listener {
//                    override fun onFirstFrameAvailable() {
//                        Timber.i("onFirstFrameAvailable")
//                    }
//
//                    override fun onCameraSwitched(newCameraId: String) {
//                        Timber.i("onCameraSwitched: newCameraId = %s", newCameraId)
//                    }
//
//                    override fun onError(
//                            camera2CapturerException: Camera2Capturer.Exception) {
//                        Timber.e(camera2CapturerException)
//                    }
//                }
//                val cameraId = Camera2Enumerator().run { deviceNames.find { isFrontFacing(it) } }
//                Camera2Capturer(context, cameraId , camera2Listener).apply {
//                    setCameraPairs(context)
//                }
//            } else {
//                val cameraId = Camera1Enumerator().run { deviceNames.find { isFrontFacing(it) } }
//                camera1Capturer = CameraCapturer(context, cameraId)
//            }
//
//        }
        }

        private fun CameraEnumerator.getFrontOrBackCameraId() =
                deviceNames.find { isFrontFacing(it) } ?: deviceNames.find { isBackFacing(it) }
    }

//    private fun usingCamera1(): Boolean {
//        return camera1Capturer != null
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun setCameraPairs(context: Context) {
//        val camera2Enumerator = Camera2Enumerator(context)
//        for (cameraId in camera2Enumerator.deviceNames) {
//            if (isCameraIdSupported(cameraId)) {
//                if (camera2Enumerator.isFrontFacing(cameraId)) {
//                    frontCameraPair = Pair<CameraCapturer, String>(cameraId)
//                }
//                if (camera2Enumerator.isBackFacing(cameraId)) {
//                    backCameraPair = Pair<CameraCapturer, String>(cameraId)
//                }
//            }
//        }
    }

//    private fun getCameraSource(cameraId: String): CameraCapturer.CameraSource {
//        return if (frontCameraPair!!.second == cameraId) {
//            frontCameraPair!!.first
//        } else {
//            backCameraPair!!.first
//        }
//    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private fun isCameraIdSupported(cameraId: String): Boolean {
//        var isMonoChromeSupported = false
//        var isPrivateImageFormatSupported = false
//        val cameraCharacteristics: CameraCharacteristics
//        cameraCharacteristics = try {
//            cameraManager!!.getCameraCharacteristics(cameraId)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return false
//        }
//        /*
//         * This is a temporary work around for a RuntimeException that occurs on devices which contain cameras
//         * that do not support ImageFormat.PRIVATE output formats. A long term fix is currently in development.
//         * https://github.com/twilio/video-quickstart-android/issues/431
//         */
//        val streamMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//        if (streamMap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            isPrivateImageFormatSupported = streamMap.isOutputSupportedFor(ImageFormat.PRIVATE)
//        }
//
//        /*
//         * Read the color filter arrangements of the camera to filter out the ones that support
//         * SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO or SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR.
//         * Visit this link for details on supported values - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_COLOR_FILTER_ARRANGEMENT
//         */
//        val colorFilterArrangement = cameraCharacteristics.get(
//                CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && colorFilterArrangement != null) {
//            isMonoChromeSupported = (colorFilterArrangement
//                    == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO
//                    || colorFilterArrangement
//                    == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR)
//        }
//        return isPrivateImageFormatSupported && !isMonoChromeSupported
//    }
}
