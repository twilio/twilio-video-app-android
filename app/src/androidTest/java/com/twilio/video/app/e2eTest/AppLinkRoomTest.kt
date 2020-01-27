package com.twilio.video.app.e2eTest

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.R
import com.twilio.video.app.getString
import com.twilio.video.app.retrieveEmailCredentials
import com.twilio.video.app.screen.assertRoomNameIsDisplayed
import com.twilio.video.app.screen.loginWithEmail
import com.twilio.video.app.ui.room.RoomActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AppLinkRoomTest {

    @Test
    fun `room_app_link_should_navigate_to_room_screen_with_room_name_populated`() {
        val roomName = "test"
        val scenario = ActivityScenario.launch<RoomActivity>(
                Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://${getString(R.string.web_app_domain)}/room/$roomName")
                )
        )
        val emailCredentials = retrieveEmailCredentials()

        loginWithEmail(emailCredentials)

        assertRoomNameIsDisplayed(roomName)

        scenario.recreate()

        assertRoomNameIsDisplayed(roomName)
    }
}