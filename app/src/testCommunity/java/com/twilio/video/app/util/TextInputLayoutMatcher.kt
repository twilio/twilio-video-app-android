package com.twilio.video.app.util

import android.view.View
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

fun isTextInputLayoutError(): Matcher<View> = object : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description?) { }

    override fun matchesSafely(item: View?): Boolean =
            if (item is TextInputLayout) item.isErrorEnabled else false
}
