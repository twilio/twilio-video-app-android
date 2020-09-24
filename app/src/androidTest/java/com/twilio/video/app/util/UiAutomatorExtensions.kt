package com.twilio.video.app.util

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import java.util.concurrent.TimeoutException

fun uiDevice(): UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun UiDevice.allowAllPermissions() {
    clickThroughDialogs("Allow|ALLOW")
}

fun UiDevice.denyAllPermissions() {
    clickThroughDialogs("Deny|DENY")
}

private fun UiDevice.clickThroughDialogs(text: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var allowPermissions = findObject(UiSelector().textMatches(text))
        if (allowPermissions.waitForExists(5000)) {
            while (allowPermissions.exists()) {
                allowPermissions.click()
                allowPermissions = findObject(UiSelector().textMatches(text))
            }
        } else {
            throw TimeoutException("Timed out while waiting for permission dialog.")
        }
    }
}
