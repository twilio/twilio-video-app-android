package com.twilio.video.app.util

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import java.util.concurrent.TimeoutException

fun uiDevice(): UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun UiDevice.allowAllPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        var allowPermissions = findText("Allow|ALLOW")
        if (allowPermissions.waitForExists(5000)) {
            while (allowPermissions.exists()) {
                allowPermissions.click()
                allowPermissions = findText("Allow|ALLOW")
            }
        } else {
            throw TimeoutException("Timed out while waiting for permission dialog.")
        }
    }
}

private fun UiDevice.findText(text: String) =
        findObject(UiSelector().textMatches(text))
