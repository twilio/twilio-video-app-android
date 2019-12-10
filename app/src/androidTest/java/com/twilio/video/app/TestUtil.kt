package com.twilio.video.app

import androidx.test.espresso.NoMatchingViewException
import java.util.concurrent.TimeoutException

fun retryViewMatcher(espressoAction: () -> Unit, timeoutInSeconds: Long = 30000L) {
    val startTime = System.currentTimeMillis()
    var currentTime = 0L
    while(currentTime <= timeoutInSeconds) {
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