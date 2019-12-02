package com.twilio.video.app.ui.login

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.DaggerTestVideoApplicationComponent
import com.twilio.video.app.TestApp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApp::class)
class LoginActivityTest {

    @get:Rule var activityScenarioRule = activityScenarioRule<LoginActivity>()

    @Test
    fun `it should successfully login with Google and navigate to the lobby screen`() {

    }
}