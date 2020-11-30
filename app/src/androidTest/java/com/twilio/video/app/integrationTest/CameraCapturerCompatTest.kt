package com.twilio.video.app.integrationTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.twilio.video.Camera2Capturer
import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.getTargetContext
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import tvi.webrtc.Camera1Enumerator
import tvi.webrtc.Camera2Enumerator
import tvi.webrtc.CameraEnumerator

@RunWith(AndroidJUnit4::class)
@MediumTest
@IntegrationTest
class CameraCapturerCompatTest {

    private var cameraEnumerator: CameraEnumerator =
            if (Camera2Capturer.isSupported(getTargetContext())) Camera2Enumerator(getTargetContext())
            else Camera1Enumerator()

    @Test
    fun it_should_return_a_null_camera_capturer_when_no_device_cameras_are_available() {
        assumeTrue(cameraEnumerator.deviceNames.isEmpty())
        val capturerCompat = CameraCapturerCompat.newInstance(getTargetContext())

        assertThat(capturerCompat, `is`(nullValue()))
    }

    @Test
    fun it_should_switch_the_camera() {
        assumeTrue(cameraEnumerator.deviceNames.isNotEmpty())
        val capturerCompat = CameraCapturerCompat.newInstance(getTargetContext())
        assertThat(capturerCompat, `is`(not(nullValue())))

        capturerCompat?.run {
            val enumerator = Camera2Enumerator(getTargetContext())
            var isFrontFacing = enumerator.isFrontFacing(cameraId)
            assertThat(isFrontFacing, equalTo(true))
            switchCamera()
            val isBackFacing = enumerator.isBackFacing(cameraId)
            assertThat(isBackFacing, equalTo(true))
            switchCamera()
            isFrontFacing = enumerator.isFrontFacing(cameraId)
            assertThat(isFrontFacing, equalTo(true))
        }
    }
}
