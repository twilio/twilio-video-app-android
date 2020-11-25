package com.twilio.video.app.util

import android.content.Context
import androidx.annotation.NonNull
import com.twilio.video.Camera2Capturer
import com.twilio.video.VideoCapturer
import timber.log.Timber

class Camera2CapturerCompat(
    context: Context,
    private val frontCameraId: String?,
    private val backCameraId: String?
) : CameraCapturerCompat {

    private val listener: Camera2Capturer.Listener? =
            object : Camera2Capturer.Listener {
                override fun onFirstFrameAvailable() {
                    Timber.i("onFirstFrameAvailable")
                }

                override fun onCameraSwitched(@NonNull newCameraId: String) {
                    Timber.i("onCameraSwitched: newCameraId = %s", newCameraId)
                }

                override fun onError(@NonNull camera2CapturerException: Camera2Capturer.Exception) {
                    Timber.e(camera2CapturerException)
                }
            }

    private val camera2Capturer: Camera2Capturer =
            Camera2Capturer(context, frontCameraId ?: backCameraId ?: "", listener)

    override val videoCapturer: VideoCapturer = camera2Capturer

    override fun switchCamera() {
        if (frontCameraId != null && backCameraId != null) {
            val newCameraId = if (camera2Capturer.cameraId == frontCameraId) backCameraId else frontCameraId
            camera2Capturer.switchCamera(newCameraId)
        } else Timber.w("Front and back cameras need to both be available in order to switch between them")
    }
}
