package com.twilio.video.app.e2eTest

import androidx.annotation.IdRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.twilio.video.app.R
import com.twilio.video.app.espresso.DrawableMatcher
import com.twilio.video.app.espresso.HiddenView
import com.twilio.video.app.screen.assertRoomIsConnected
import com.twilio.video.app.screen.clickDisconnectButton
import com.twilio.video.app.screen.clickJoinRoomButton
import com.twilio.video.app.screen.clickMicButton
import com.twilio.video.app.screen.clickVideoButton
import com.twilio.video.app.screen.enterRoomName
import com.twilio.video.app.ui.splash.SplashActivity
import com.twilio.video.app.util.assertTextIsDisplayedRetry
import com.twilio.video.app.util.clickView
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.getTargetContext
import com.twilio.video.app.util.randomUUID
import com.twilio.video.app.util.retryEspressoAction
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
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
    fun it_should_toggle_the_local_video_correctly_while_pinned() {
        enterRoomName(randomUUID())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }

        onView(allOf(withText(getString(R.string.you)), isDisplayed()))
                .perform(click())
        clickView(R.id.local_video_image_button)
        retryEspressoAction {
            onView(allOf(withId(R.id.participant_stub_image),
                    withContentDescription(getString(R.string.primary_profile_picture))))
                        .check(matches(isDisplayed()))
        }
        onView(allOf(withId(R.id.participant_stub_image),
                withContentDescription(getString(R.string.profile_picture)))).check(matches(isDisplayed()))
        clickView(R.id.local_video_image_button)
        retryEspressoAction {
            onView(allOf(withId(R.id.participant_stub_image),
                    withContentDescription(getString(R.string.primary_profile_picture))))
                        .check(HiddenView())
        }
        onView(allOf(withId(R.id.participant_stub_image),
                withContentDescription(getString(R.string.profile_picture)))).check(HiddenView())

        clickDisconnectButton()
    }

    @Test
    fun it_should_toggle_the_local_tracks_correctly_in_lobby() {
        drawableIsDisplayed(R.id.local_video_image_button,
                DrawableMatcher(getTargetContext(), R.drawable.ic_videocam_white_24px))
        drawableIsDisplayed(R.id.local_audio_image_button,
                DrawableMatcher(getTargetContext(), R.drawable.ic_mic_white_24px))

        clickView(R.id.local_video_image_button)

        drawableIsDisplayed(R.id.local_video_image_button,
                DrawableMatcher(getTargetContext(), R.drawable.ic_videocam_off_gray_24px))
        onView(withId(R.id.participant_stub_image)).check(matches(isDisplayed()))
        onView(withId(R.id.participant_video)).check(HiddenView())

        clickView(R.id.local_video_image_button)

        drawableIsDisplayed(R.id.local_video_image_button,
                DrawableMatcher(getTargetContext(), R.drawable.ic_videocam_white_24px))
        onView(withId(R.id.participant_stub_image)).check(HiddenView())
        onView(withId(R.id.participant_video)).check(matches(isDisplayed()))

        clickView(R.id.local_audio_image_button)

        drawableIsDisplayed(R.id.local_audio_image_button,
                DrawableMatcher(getTargetContext(), R.drawable.ic_mic_off_gray_24px))

        clickView(R.id.local_audio_image_button)

        drawableIsDisplayed(R.id.local_audio_image_button,
                DrawableMatcher(getTargetContext(), R.drawable.ic_mic_white_24px))
    }

    @Test
    fun it_should_connect_to_a_room_successfully_with_mic_and_video_muted() {
        clickVideoButton()
        clickMicButton()
        val roomName = randomUUID()
        enterRoomName(roomName)
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }
        onView(allOf(withText(roomName), isDisplayed()))
                .check(matches(isDisplayed()))

        clickDisconnectButton()
    }

    @Test
    fun it_should_display_the_speakerphone_icon_when_selected() {
        val speakerphoneDrawableMatcher = DrawableMatcher(getTargetContext(),
                R.drawable.ic_volume_up_white_24dp)

        onView(withId(R.id.device_menu_item)).perform(click())

        val speakerphoneName = "Speakerphone"
        assertTextIsDisplayedRetry(speakerphoneName)

        clickView(speakerphoneName)
        retryEspressoAction { drawableIsDisplayed(R.id.device_menu_item, speakerphoneDrawableMatcher) }

        enterRoomName(randomUUID())
        clickJoinRoomButton()

        retryEspressoAction { assertRoomIsConnected() }
        drawableIsDisplayed(R.id.device_menu_item, speakerphoneDrawableMatcher)

        clickDisconnectButton()
    }

    private fun drawableIsDisplayed(@IdRes id: Int, earpieceDrawableMatcher: DrawableMatcher) {
        retryEspressoAction {
            onView(withId(id)).check(matches(earpieceDrawableMatcher))
        }
    }
}
