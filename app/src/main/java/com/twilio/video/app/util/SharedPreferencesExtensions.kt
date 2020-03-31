package com.twilio.video.app.util

import android.content.SharedPreferences

fun SharedPreferences.remove(key: String) {
    edit()
        .remove(key)
        .apply()
}

fun SharedPreferences.putString(key: String, value: String) {
    edit()
        .putString(key, value)
        .apply()
}