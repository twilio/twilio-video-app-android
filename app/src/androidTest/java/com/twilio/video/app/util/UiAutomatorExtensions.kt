package com.twilio.video.app.util

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import timber.log.Timber

fun uiDevice(): UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun UiDevice.clearTask(swipeAttempts: Int = 10 ) {
    pressRecentApps()

    var currentAttempt = 1
    val centerScreenX = displayWidth / 2
    val centerScreenY = displayHeight / 2
    while (currentAttempt <= swipeAttempts) {
        Timber.d("Clear all tasks attempt $currentAttempt")
        swipe(centerScreenX, centerScreenY, displayWidth, centerScreenY, 50)
        val uiObject = findObject(UiSelector().text("Clear all"))
        if (uiObject.exists()) {
            uiObject.click()
            break
        } else {
            currentAttempt++
        }
    }
}