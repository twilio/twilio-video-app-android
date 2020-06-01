package com.twilio.video.app.security

class SecurePreferencesFake : SecurePreferences {

    private var preferences: MutableMap<String, String> = mutableMapOf()

    override fun putSecureString(key: String, value: String) {
        preferences[key] = value
    }

    override fun getSecureString(key: String) = preferences[key]
}
