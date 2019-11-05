package com.twilio.video.app.auth

import android.content.SharedPreferences
import com.twilio.video.app.base.BaseActivity
import com.twilio.video.app.data.Preferences
import com.twilio.video.app.ui.login.CommunityLoginActivity

class CommunityAuthenticator(private val preferences: SharedPreferences) : Authenticator {

    override fun getLoginActivity(): Class<out BaseActivity> {
        return CommunityLoginActivity::class.java
    }

    override fun loggedIn(): Boolean {
        return !preferences.getString(Preferences.DISPLAY_NAME, null).isNullOrEmpty()
    }

    override fun logout() {
        preferences.edit().remove(Preferences.DISPLAY_NAME).apply()
    }
}
