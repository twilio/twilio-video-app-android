package com.twilio.video.app.e2eTest

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.twilio.video.app.HiddenView
import com.twilio.video.app.R
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.allowAllPermissions
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.retryEspressoAction
import com.twilio.video.app.util.uiDevice
import org.junit.Rule
import org.junit.Test

class PermissionTest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun `it_should_render_the_local_video_track_before_connecting_to_a_room`() {
        loginWithEmail(retrieveEmailCredentials())

        uiDevice().run {
            allowAllPermissions()
        }

        retryEspressoAction { onView(withId(R.id.participant_stub_image))
                .check(HiddenView()) }
    }
}
