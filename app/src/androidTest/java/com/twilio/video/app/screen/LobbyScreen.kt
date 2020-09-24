package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.twilio.video.app.R
import com.twilio.video.app.espresso.HiddenView
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.getTargetContext
import org.hamcrest.CoreMatchers.not

fun assertScreenIsDisplayed() {
    onView(withText(getString(R.string.join)))
            .check(matches(isDisplayed()))
            .check(matches(not(isEnabled())))
}

fun clickSettingsMenuItem() {
    openActionBarOverflowOrOptionsMenu(getTargetContext())
    onView(withText(getString(R.string.settings_title))).perform(click())
}

fun assertRoomNameIsDisplayed(roomName: String) {
    onView(withText(roomName)).check(matches(isDisplayed()))
}

fun enterRoomName(roomName: String) {
    onView(withId(R.id.room_edit_text)).perform(typeText(roomName))
}

fun clickVideoButton() {
    onView(withId(R.id.local_video_image_button)).perform(click())
}

fun clickMicButton() {
    onView(withId(R.id.local_audio_image_button)).perform(click())
}

fun clickJoinRoomButton() {
    onView(withId(R.id.connect)).perform(click())
}

fun assertRoomIsConnected() {
    onView(withId(R.id.remote_video_thumbnails)).check(matches(hasChildCount(1)))
}

fun clickDisconnectButton() {
    onView(withId(R.id.disconnect)).perform(click())
}

fun assertParticipantStubIsHidden() {
    onView(withId(R.id.participant_stub_image))
        .check(HiddenView())
}

fun assertVideoButtonIsDisabled() {
    onView(withId(R.id.local_video_image_button)).check(matches(not(isEnabled())))
}

fun assertMicButtonIsDisabled() {
    onView(withId(R.id.local_audio_image_button)).check(matches(not(isEnabled())))
}

fun assertVideoButtonIsEnabled() {
    onView(withId(R.id.local_video_image_button)).check(matches(isEnabled()))
}

fun assertMicButtonIsEnabled() {
    onView(withId(R.id.local_audio_image_button)).check(matches(isEnabled()))
}
