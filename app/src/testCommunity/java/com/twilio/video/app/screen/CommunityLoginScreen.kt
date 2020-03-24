package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.twilio.video.app.R
import com.twilio.video.app.util.isTextInputLayoutError
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.robolectric.shadows.ShadowDialog.getLatestDialog

fun enterYourName(name: String) {
    onView(withId(R.id.community_login_screen_name_edittext)).perform(clearText(), typeText(name))
}

fun clickLoginButton() {
    onView(withId(R.id.community_login_screen_login_button)).perform(click())
}

fun assertLoadingIndicatorIsDisplayed() {
    onView(withId(R.id.community_login_screen_progressbar)).check(matches(isDisplayed()))
}

fun assertLoadingIndicatorIsNotDisplayed() {
    onView(withId(R.id.community_login_screen_progressbar)).check(matches(not(isDisplayed())))
}

fun assertLoginButtonIsEnabled() {
    onView(withId(R.id.community_login_screen_login_button)).check(matches(isEnabled()))
}

fun assertLoginButtonIsDisabled() {
    onView(withId(R.id.community_login_screen_login_button)).check(matches(not(isEnabled())))
}

fun assertInvalidPasscodeErrorIsDisplayed() {
    onView(withText(R.string.login_screen_invalid_passcode_error)).check(matches(isDisplayed()))
}

fun assertExpiredPasscodeErrorIsDisplayed() {
    onView(withText(R.string.login_screen_expired_passcode_error)).check(matches(isDisplayed()))
}

fun assertThatPasscodeErrorIsDisabled() {
    onView(withId(R.id.community_login_screen_passcode)).check(matches(not(isTextInputLayoutError())))
}

fun assertErrorDialogIsDisplayed() {
    getLatestDialog().let { dialog ->
        assertThat(dialog, `is`(notNullValue()))
        assertThat(dialog.isShowing, equalTo(true))
    }
}
