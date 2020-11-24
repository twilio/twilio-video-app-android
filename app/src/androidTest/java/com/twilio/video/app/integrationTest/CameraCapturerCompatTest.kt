package com.twilio.video.app.integrationTest

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.getTargetContext
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import tvi.webrtc.Camera2Enumerator

@RunWith(AndroidJUnit4::class)
@MediumTest
@IntegrationTest
class CameraCapturerCompatTest {

    @Test
    fun it_should_return_a_null_camera_capturer_when_no_device_cameras_are_available() {
        assumeTrue(Camera2Enumerator(getTargetContext()).run { deviceNames.isEmpty() })
        val capturerCompat = CameraCapturerCompat.newInstance(getTargetContext())
        assertThat(capturerCompat, `is`(nullValue()))
    }
}
