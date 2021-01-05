package com.twilio.video.app.e2eTest

import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.util.allowAllPermissions
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.uiDevice
import org.junit.Before

@E2ETest
open class BaseE2ETest {
    @Before
    open fun setUp() {
        loginWithEmail(retrieveEmailCredentials())
        uiDevice().run {
            allowAllPermissions()
        }
    }
}
