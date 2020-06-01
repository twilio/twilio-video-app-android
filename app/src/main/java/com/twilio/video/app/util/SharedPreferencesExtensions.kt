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

/*
 * Utility method that allows getting a shared preference with a default value. The return value
 * type is inferred by the default value type.
 */
inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    return when (defaultValue) {
        is Boolean -> getBoolean(key, defaultValue) as T
        is Float -> getFloat(key, defaultValue) as T
        is Int -> getInt(key, defaultValue) as T
        is Long -> getLong(key, defaultValue) as T
        is String -> getString(key, defaultValue) as T
        else -> throw IllegalArgumentException("Attempted to get preference with unsupported type")
    }
}
