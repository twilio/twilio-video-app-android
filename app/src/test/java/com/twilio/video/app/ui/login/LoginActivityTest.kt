package com.twilio.video.app.ui.login

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.DaggerTestVideoApplicationComponent
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class LoginActivityTest {

    companion object {

        @JvmStatic
        @BeforeClass
        fun beforeClass() {
            DaggerTestVideoApplicationComponent
                    .builder()
                    .applicationModule(ApplicationModule(ApplicationProvider.getApplicationContext()))
                    .build()
        }
    }

    @get:Rule var activityScenarioRule = activityScenarioRule<LoginActivity>()

    @Test
    fun `it should successfully login with Google and navigate to the lobby screen`() {
    }
}