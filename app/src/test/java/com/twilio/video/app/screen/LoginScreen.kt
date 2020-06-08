package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.R

fun clickGoogleSignInButton() {
    onView(withId(R.id.google_sign_in_button)).perform(click())
}
