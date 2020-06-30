package com.twilio.video.app.e2eTest

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.twilio.video.app.screen.assertMicButtonIsDisabled
import com.twilio.video.app.screen.assertMicButtonIsEnabled
import com.twilio.video.app.screen.assertParticipantStubIsHidden
import com.twilio.video.app.screen.assertVideoButtonIsDisabled
import com.twilio.video.app.screen.assertVideoButtonIsEnabled
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.allowAllPermissions
import com.twilio.video.app.util.denyAllPermissions
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.retryEspressoAction
import com.twilio.video.app.util.uiDevice
import org.junit.Rule
import org.junit.Test

@E2ETest
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

        retryEspressoAction { assertParticipantStubIsHidden() }
        assertVideoButtonIsEnabled()
        assertMicButtonIsEnabled()
    }

    @Test
    fun `it_should_display_disabled_mic_and_camera_buttons_when_permissions_are_denied`() {
        loginWithEmail(retrieveEmailCredentials())

        uiDevice().run {
            denyAllPermissions()
        }

        retryEspressoAction { assertVideoButtonIsDisabled() }
        assertMicButtonIsDisabled()
    }
}
