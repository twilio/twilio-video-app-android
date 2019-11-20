package com.twilio.video.app.auth

sealed class LoginIntentEvent {
    object GoogleSignInIntent : LoginIntentEvent()
}
