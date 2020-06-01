package com.twilio.video.app.auth

import com.twilio.video.app.data.api.AuthServiceError

sealed class CommunityLoginResult : LoginResult {
    data class CommunityLoginFailureResult(val error: AuthServiceError? = null) : CommunityLoginResult()
    object CommunityLoginSuccessResult : CommunityLoginResult()
}
