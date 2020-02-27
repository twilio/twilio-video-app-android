package com.twilio.video.app.screen

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.R

fun enterYourName(name: String) {
    onView(withId(R.id.community_login_screen_name_edittext)).perform(typeText(name))
}