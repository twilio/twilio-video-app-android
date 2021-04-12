package com.twilio.video.app.integrationTest

import com.twilio.video.app.util.CameraCapturerCompat
import com.twilio.video.app.util.getTargetContext
import org.junit.Assume.assumeTrue
import org.junit.Before

open class BaseIntegrationTest {

    @Before
    fun setUp() {
        // Skip any devices that don't have a front or back camera
        assumeTrue(CameraCapturerCompat.newInstance(getTargetContext()) != null)
    }
}
