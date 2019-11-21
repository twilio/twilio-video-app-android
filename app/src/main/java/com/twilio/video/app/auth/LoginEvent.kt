package com.twilio.video.app.auth

import android.content.Intent

sealed class LoginEvent {
    data class TokenLogin(val identity: String) : LoginEvent()
    data class EmailLogin(val email: String, val password: String) : LoginEvent()
    data class GoogleLogin(val signInResultIntent: Intent) : LoginEvent()
}
