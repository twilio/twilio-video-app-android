package com.twilio.video.app.security

interface SecurePreferences {

    fun putSecureString(key: String, value: String)

    fun getSecureString(key: String): String?
}
