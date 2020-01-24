package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.twilio.video.app.R
import com.twilio.video.app.retrieveEmailCredentials

// TODO Move to common module as part of https://issues.corp.twilio.com/browse/AHOYAPPS-197

fun assertGoogleSignInButtonIsVisible() {
    onView(withId(R.id.google_sign_in_button)).check(matches(isDisplayed()))
}

fun assertSignInErrorIsVisible() {
    val errorTitle = getInstrumentation().targetContext.getString(R.string.login_screen_auth_error_title)
    onView(withText(errorTitle)).check(matches(isDisplayed()))
}

fun loginWithEmail() {
    val emailCredentials = retrieveEmailCredentials()
    loginWithEmail(emailCredentials.email, emailCredentials.password)
}

fun loginWithWrongEmailCreds() {
    val emailCredentials = retrieveEmailCredentials()
    loginWithEmail(emailCredentials.email, "foo")
}

private fun loginWithEmail(email: String, password: String) {
    onView(withId(R.id.email_sign_in_button)).perform(click())
    onView(withId(R.id.email_edittext)).perform(typeText(email))
    onView(withId(R.id.password_edittext)).perform(scrollTo(), typeText(password))
    onView(withId(R.id.login_button)).perform(click())
}
