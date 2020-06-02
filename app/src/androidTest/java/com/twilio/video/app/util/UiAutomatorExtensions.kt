package com.twilio.video.app.util

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector

fun uiDevice(): UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun UiDevice.allowPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val device = uiDevice()
        val allowPermissions = device.findObject(UiSelector().textMatches("Allow|ALLOW"))
        if (allowPermissions.exists()) {
            allowPermissions.click()
        }
    }
}
