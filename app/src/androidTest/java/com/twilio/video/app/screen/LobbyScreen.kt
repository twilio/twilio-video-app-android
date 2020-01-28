package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.twilio.video.app.R
import com.twilio.video.app.getString
import com.twilio.video.app.getTargetContext

fun clickSettingsMenuItem() {
    openActionBarOverflowOrOptionsMenu(getTargetContext())
    onView(withText(getString(R.string.settings))).perform(click())
}

fun assertRoomNameIsDisplayed(roomName: String) {
    onView(withText(roomName)).check(matches(isDisplayed()))
}

fun clickJoinRoomButton() {
    onView(withId(R.id.connect)).perform(click())
}

fun assertJoiningRoomIsDisplayed() {
    onView(withText("Joining...")).check(matches(isDisplayed()))
}

fun clickDisconnectButton() {
    onView(withId(R.id.disconnect)).perform(click())
}

