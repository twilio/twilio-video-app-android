package com.twilio.video.app.espresso

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import java.lang.RuntimeException

class HiddenView : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        noViewFoundException?.let { throw it }
        val isNotVisible = view?.let { it.visibility == View.GONE } ?: false
        if (isNotVisible) return else throw RuntimeException()
    }
}
