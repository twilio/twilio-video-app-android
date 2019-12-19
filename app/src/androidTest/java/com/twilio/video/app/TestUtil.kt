package com.twilio.video.app

import androidx.test.espresso.NoMatchingViewException
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