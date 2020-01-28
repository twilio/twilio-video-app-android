package com.twilio.video.app.e2eTest

import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.twilio.video.app.R
import com.twilio.video.app.getString
import com.twilio.video.app.retrieveEmailCredentials
import com.twilio.video.app.retryViewMatcher
import com.twilio.video.app.screen.*
import com.twilio.video.app.ui.room.RoomActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppLinkRoomTest {

    @get:Rule
    var rule: ActivityTestRule<RoomActivity> =
            ActivityTestRule(RoomActivity::class.java,
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

        retryViewMatcher { assertRoomNameIsDisplayed(roomName) }

        restartActivity(intent)

        retryViewMatcher { assertRoomNameIsDisplayed(roomName) }

        retryViewMatcher { clickSettingsMenuItem() }
        logout()
    }

    private fun restartActivity(intent: Intent) {
        rule.finishActivity()
        rule.launchActivity(intent)
    }
}