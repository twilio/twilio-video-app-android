package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.R
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.getTargetContext
import org.hamcrest.CoreMatchers.allOf

fun assertScreenIsDisplayed() {
    onView(withText(getString(R.string.join))).check(matches(isDisplayed()))
}

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
    onView(withText(getString(R.string.you))).check(matches(isDisplayed()))
}

fun clickDisconnectButton() {
    onView(withId(R.id.disconnect)).perform(click())
}
