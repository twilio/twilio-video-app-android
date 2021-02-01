package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.EmailCredentials
import com.twilio.video.app.R
import com.twilio.video.app.util.retryEspressoAction

// TODO Move to common module as part of https://issues.corp.twilio.com/browse/AHOYAPPS-197

fun loginWithEmail(emailCredentials: EmailCredentials) {
    loginWithEmail(emailCredentials.email, emailCredentials.password)
}

private fun loginWithEmail(email: String, password: String) {
    onView(withId(R.id.email_button)).perform(click())
    onView(withId(R.id.email)).perform(typeText(email))
    onView(withId(R.id.button_next)).perform(click())
    retryEspressoAction { onView(withId(R.id.password)).perform(typeText(password)) }
    onView(withId(R.id.button_done)).perform(click())
}
