package com.twilio.video.app.e2eTest

import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.retryEspressoAction
import com.twilio.video.app.screen.clickSettingsMenuItem
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.screen.logout
import org.junit.After
import org.junit.Before

open class BaseUITest {

    @Before
    fun setUp() {
        loginWithEmail(retrieveEmailCredentials())
    }

    @After
    fun tearDown() {
        retryEspressoAction { clickSettingsMenuItem() }
        logout()
    }
}