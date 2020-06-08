package com.twilio.audioswitch.android

import android.util.Log

internal class LogWrapper {

    fun d(tag: String?, message: String) {
        Log.d(tag, message)
    }

    fun e(tag: String?, message: String) {
        Log.e(tag, message)
    }
}
