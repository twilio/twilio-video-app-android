package com.twilio.video.app.screen

import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.twilio.video.app.R
import com.twilio.video.app.util.getString
import com.twilio.video.app.util.retryEspressoAction

fun assertSettingsTitleIsVisible() {
    retryEspressoAction { onView(withText(getString(R.string.settings))).check(matches(isDisplayed())) }
}

fun logout() {
    onView(withId(androidx.preference.R.id.recycler_view))
            .perform(actionOnItem<ViewHolder>(
                    hasDescendant(withText(
                            getString(R.string.settings_screen_logout))), click()))
}