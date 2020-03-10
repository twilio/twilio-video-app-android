package com.twilio.video.app.ui.login

sealed class CommunityLoginViewEvent {
    data class IdentityTextChanged(val text: String): CommunityLoginViewEvent()
    data class PasscodeTextChanged(val text: String): CommunityLoginViewEvent()
}