package com.twilio.video.app.screen

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.twilio.video.app.R
import com.twilio.video.app.getString
import com.twilio.video.app.getTargetContext

fun clickSettingsMenuItem() {
    Espresso.openActionBarOverflowOrOptionsMenu(getTargetContext())
    Espresso.onView(ViewMatchers.withText(getString(R.string.settings))).perform(ViewActions.click())
}

fun assertRoomNameIsDisplayed(roomName: String) {
    onView(withText(R.string.room)).check(matches(isDisplayed()))
}