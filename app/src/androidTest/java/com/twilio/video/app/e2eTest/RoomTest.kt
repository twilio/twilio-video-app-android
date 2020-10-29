package com.twilio.video.app.e2eTest

import android.content.Context
import android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET
import android.media.AudioManager
import android.media.AudioManager.GET_DEVICES_INPUTS
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.R
import com.twilio.video.app.espresso.DrawableMatcher
import com.twilio.video.app.screen.assertRoomIsConnected
import com.twilio.video.app.screen.clickDisconnectButton
import com.twilio.video.app.screen.clickJoinRoomButton
import com.twilio.video.app.screen.clickMicButton
import com.twilio.video.app.screen.clickVideoButton
import com.twilio.video.app.screen.enterRoomName
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.assertTextIsDisplayedRetry
import com.twilio.video.app.util.clickView
import com.twilio.video.app.util.getTargetContext
import com.twilio.video.app.util.randomUUID
import com.twilio.video.app.util.retryEspressoAction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@E2ETest
class RoomTest : BaseE2ETest() {

    @get:Rule
    var scenario = activityScenarioRule<SplashActivity>()

    @Test
    fun it_should_connect_to_a_room_successfully() {
        enterRoomName(randomUUID())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }

        clickDisconnectButton()
    }

    @Test
    fun it_should_connect_to_a_room_successfully_with_mic_and_video_muted() {
        clickVideoButton()
        clickMicButton()
        enterRoomName(randomUUID())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }

        clickDisconnectButton()
    }

    @Test
    fun it_should_display_the_earpiece_icon_by_default() {
        val defaultDrawableMatcher = getDefaultDrawableMatcher()
        drawableIsDisplayed(defaultDrawableMatcher)

        enterRoomName(randomUUID())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }
        drawableIsDisplayed(defaultDrawableMatcher)

        clickDisconnectButton()
    }

    @Test
    fun it_should_display_the_speakerphone_icon_when_selected() {
        val defaultDrawableMatcher = getDefaultDrawableMatcher()
        val speakerphoneDrawableMatcher = DrawableMatcher(getTargetContext(),
                R.drawable.ic_volume_up_white_24dp)
        drawableIsDisplayed(defaultDrawableMatcher)

        onView(withId(R.id.device_menu_item)).perform(click())

        val speakerphoneName = "Speakerphone"
        assertTextIsDisplayedRetry(speakerphoneName)

        clickView(speakerphoneName)
        retryEspressoAction { drawableIsDisplayed(speakerphoneDrawableMatcher) }

        enterRoomName(randomUUID())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }
        drawableIsDisplayed(speakerphoneDrawableMatcher)

        clickDisconnectButton()
    }

    private fun getDefaultDrawableMatcher(): DrawableMatcher {
        /*
         * Some FTL devices have headsets plugged in so we need to check for the default audio device
         */
        val audioManager = getTargetContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        requireNotNull(audioManager)
        val drawable = audioManager.getDevices(GET_DEVICES_INPUTS)
                .find { it.type == TYPE_WIRED_HEADSET }
                ?.let { R.drawable.ic_headset_mic_white_24dp }
                ?: R.drawable.ic_phonelink_ring_white_24dp
        return DrawableMatcher(getTargetContext(), drawable)
    }

    private fun drawableIsDisplayed(earpieceDrawableMatcher: DrawableMatcher) {
        retryEspressoAction {
            onView(withId(R.id.device_menu_item)).check(matches(earpieceDrawableMatcher))
        }
    }
}
