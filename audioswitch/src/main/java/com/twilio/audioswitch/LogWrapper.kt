package com.twilio.audioswitch

import android.util.Log

class LogWrapper {

    fun d(tag: String?, message: String) {
        Log.d(tag, message)
    }

    fun e(tag: String?, message: String) {
        Log.e(tag, message)
    }
}