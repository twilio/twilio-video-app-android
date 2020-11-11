package com.twilio.video.app.util

import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.not

fun clickView(viewText: String) {
    onView(withText(viewText)).perform(click())
}

fun clickView(@IdRes id: Int) {
    onView(withId(id)).perform(click())
}

fun scrollAndClickView(viewText: String, recyclerViewId: Int) {
    onView(withId(recyclerViewId))
            .perform(actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText(viewText)), click()))
}

fun assertTextIsDisplayed(text: String) {
    onView(withText(text)).check(matches(isDisplayed()))
}

fun assertTextIsDisplayedRetry(text: String) {
    retryEspressoAction { onView(withText(text)).check(matches(isDisplayed())) }
}

fun assertTextIsNotDisplayedRetry(text: String) {
    retryEspressoAction { onView(withText(text)).check(matches(not(isDisplayed()))) }
}
