package com.twilio.video.app

import androidx.annotation.IdRes
import androidx.test.espresso.NoMatchingViewException
import androidx.test.platform.app.InstrumentationRegistry
import java.util.concurrent.TimeoutException

fun retryViewMatcher(timeoutInSeconds: Long = 60000L, espressoAction: () -> Unit) {
    val startTime = System.currentTimeMillis()
    var currentTime = 0L
    while (currentTime <= timeoutInSeconds) {
        try {
            espressoAction()
            return
        } catch (e: NoMatchingViewException) {
            currentTime = System.currentTimeMillis() - startTime
            Thread.sleep(10)
        }
    }
    throw TimeoutException("Timeout occurred while attempting to find a matching view")
}

fun getString(@IdRes stringId: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(stringId)