package com.twilio.audioswitch.android

import android.util.Log

private const val TAG_PREFIX = "AS/"

internal class Logger(var loggingEnabled: Boolean = false) {

    fun d(tag: String, message: String) {
        if (loggingEnabled) {
            Log.d(createTag(tag), message)
        }
    }

    fun w(tag: String, message: String) {
        if (loggingEnabled) {
            Log.w(createTag(tag), message)
        }
    }

    fun e(tag: String, message: String) {
        if (loggingEnabled) {
            Log.e(createTag(tag), message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        if (loggingEnabled) {
            Log.e(createTag(tag), message, throwable)
        }
    }

    private fun createTag(tag: String): String {
        return "$TAG_PREFIX$tag"
    }
}
