package com.twilio.video.app.e2eTest

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.twilio.video.app.R
import com.twilio.video.app.screen.assertRoomNameIsDisplayed
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.retrieveEmailCredentials
import com.twilio.video.app.util.retryEspressoAction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@E2ETest
class AppLinkRoomTest {

    @get:Rule
    var permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @get:Rule
    var rule: ActivityTestRule<SplashActivity> =
            ActivityTestRule(SplashActivity::class.java,
            true,
            false)

    @Test
    fun `room_app_link_should_navigate_to_room_screen_with_room_name_populated`() {
        val roomName = "test"
        val intent = Intent(Intent.ACTION_VIEW,
                Uri.parse("https://${getString(R.string.web_app_domain)}/room/$roomName")
        )
        rule.launchActivity(intent)

        val emailCredentials = retrieveEmailCredentials()

        loginWithEmail(emailCredentials)

        retryEspressoAction { assertRoomNameIsDisplayed(roomName) }

        restartActivity(intent)

        retryEspressoAction { assertRoomNameIsDisplayed(roomName) }
    }

    private fun restartActivity(intent: Intent) {
        rule.finishActivity()
        rule.launchActivity(intent)
    }
}
