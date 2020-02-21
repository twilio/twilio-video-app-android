package com.twilio.video.app.util

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

fun uiDevice(): UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())