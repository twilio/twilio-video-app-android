package com.twilio.video.app.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.twilio.video.app.data.api.AuthServiceError

sealed class LoginResult {
    data class GoogleLoginIntentResult(val intent: Intent) : LoginResult()
    data class GoogleLoginSuccessResult(val googleSignInAccount: GoogleSignInAccount) : LoginResult()
    data class EmailLoginSuccessResult(val email: String) : LoginResult()
    data class CommunityLoginFailureResult(val error: AuthServiceError? = null) : LoginResult()
    object CommunityLoginSuccessResult : LoginResult()
}