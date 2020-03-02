package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.R

fun enterYourName(name: String) {
    onView(withId(R.id.community_login_screen_name_edittext)).perform(typeText(name))
}

fun enterYourPasscode(passcode: String) {
    onView(withId(R.id.community_login_screen_passcode_edittext)).perform(typeText(passcode))
}

fun clickLoginButton() {
    onView(withId(R.id.community_login_screen_login_button)).perform(click())
}

fun assertLoadingIndicatorIsDisplayed() {
    onView(withId(R.id.community_login_screen_progressbar)).check(matches(isDisplayed()))
}