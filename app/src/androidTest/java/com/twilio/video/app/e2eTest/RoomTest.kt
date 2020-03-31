package com.twilio.video.app.e2eTest

import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.screen.assertRoomIsConnected
import com.twilio.video.app.screen.assertScreenIsDisplayed
import com.twilio.video.app.screen.clickDisconnectButton
import com.twilio.video.app.screen.clickJoinRoomButton
import com.twilio.video.app.screen.clickMicButton
import com.twilio.video.app.screen.clickVideoButton
import com.twilio.video.app.screen.enterRoomName
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.retryEspressoAction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@LargeTest
class RoomTest : BaseUITest() {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun it_should_connect_to_a_room_successfully() {
        retryEspressoAction { assertScreenIsDisplayed() }

        enterRoomName(UUID.randomUUID().toString())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }

        clickDisconnectButton()
    }

    @Test
    fun it_should_connect_to_a_room_successfully_with_mic_and_video_muted() {
        retryEspressoAction { assertScreenIsDisplayed() }

        clickVideoButton()
        clickMicButton()
        enterRoomName(UUID.randomUUID().toString())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }

        clickDisconnectButton()
    }
}