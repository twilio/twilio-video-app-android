package com.twilio.video.app.auth

import android.content.Intent

sealed class LoginResult {
    data class GoogleLoginIntentResult(val intent: Intent) : LoginResult()
    object GoogleLoginSuccessResult : LoginResult()
    object EmailLoginSuccessResult : LoginResult()
    object CommunityLoginSuccessResult : LoginResult()
}