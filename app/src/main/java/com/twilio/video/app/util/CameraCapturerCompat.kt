package com.twilio.video.app.util

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import com.twilio.video.Camera2Capturer
import com.twilio.video.VideoCapturer
import timber.log.Timber
import tvi.webrtc.Camera1Enumerator
import tvi.webrtc.Camera2Enumerator
import tvi.webrtc.CameraEnumerator

interface CameraCapturerCompat {
    val videoCapturer: VideoCapturer
    fun switchCamera()

    companion object {
        fun newInstance(context: Context): CameraCapturerCompat? {
            return if (Camera2Capturer.isSupported(context)) {
                Camera2Enumerator(context).getFrontAndBackCameraIds(context)?.let { cameraIds ->
                    Camera2CapturerCompat(context, cameraIds.first, cameraIds.second)
                }
            } else {
                Camera1Enumerator().getFrontAndBackCameraIds(context)?.let { cameraIds ->
                    LegacyCameraCapturerCompat(context, cameraIds.first, cameraIds.second)
                }
            }
        }

        private fun CameraEnumerator.getFrontAndBackCameraIds(context: Context): Pair<String?, String?>? {
            val cameraIds = deviceNames.find { isFrontFacing(it) && isCameraIdSupported(context, it) } to
                    deviceNames.find { isBackFacing(it) && isCameraIdSupported(context, it) }
            return if (isAtLeastOneCameraAvailable(cameraIds.first, cameraIds.second)) cameraIds
            else {
                Timber.w("No cameras are available on this device")
                null
            }
        }

        private fun isCameraIdSupported(context: Context, cameraId: String): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                var isMonoChromeSupported = false
                var isPrivateImageFormatSupported = false
                val cameraCharacteristics: CameraCharacteristics
                cameraCharacteristics = try {
                    cameraManager.getCameraCharacteristics(cameraId)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            /*
             * This is a temporary work around for a RuntimeException that occurs on devices which contain cameras
             * that do not support ImageFormat.PRIVATE output formats. A long term fix is currently in development.
             * https://github.com/twilio/video-quickstart-android/issues/431
             */
                val streamMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                if (streamMap != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    isPrivateImageFormatSupported = streamMap.isOutputSupportedFor(ImageFormat.PRIVATE)
                }

            /*
             * Read the color filter arrangements of the camera to filter out the ones that support
             * SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO or SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR.
             * Visit this link for details on supported values - https://developer.android.com/reference/android/hardware/camera2/CameraCharacteristics#SENSOR_INFO_COLOR_FILTER_ARRANGEMENT
             */
                val colorFilterArrangement = cameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && colorFilterArrangement != null) {
                    isMonoChromeSupported = (colorFilterArrangement
                            == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_MONO ||
                            colorFilterArrangement
                            == CameraMetadata.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT_NIR)
                }
                isPrivateImageFormatSupported && !isMonoChromeSupported
            } else true
        }

        private fun isAtLeastOneCameraAvailable(frontCameraId: String?, backCameraId: String?) =
                frontCameraId != null || backCameraId != null
    }
}
