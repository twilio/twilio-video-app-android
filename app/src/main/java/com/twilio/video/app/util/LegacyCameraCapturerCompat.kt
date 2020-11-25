package com.twilio.video.app.util

import android.content.Context
import com.twilio.video.CameraCapturer
import com.twilio.video.VideoCapturer
import timber.log.Timber

class LegacyCameraCapturerCompat(
    context: Context,
    private val frontCameraId: String?,
    private val backCameraId: String?
) : CameraCapturerCompat {

    private val cameraCapturer: CameraCapturer =
            CameraCapturer(context, frontCameraId ?: backCameraId ?: "")
    override val videoCapturer: VideoCapturer = cameraCapturer

    override fun switchCamera() {
        if (frontCameraId != null && backCameraId != null) {
            val newCameraId = if (cameraCapturer.cameraId == frontCameraId) backCameraId else frontCameraId
            cameraCapturer.switchCamera(newCameraId)
        } else Timber.w("Front and back cameras need to both be available in order to switch between them")
    }
}
