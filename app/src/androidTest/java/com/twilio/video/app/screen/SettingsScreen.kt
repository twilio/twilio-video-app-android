package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import com.twilio.video.app.R
import com.twilio.video.app.getString
import com.twilio.video.app.retryViewMatcher

fun assertSettingsTitleIsVisible() {
    retryViewMatcher { onView(withText(getString(R.string.settings))).check(matches(isDisplayed())) }
}