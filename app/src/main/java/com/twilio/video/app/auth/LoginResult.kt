package com.twilio.video.app.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class LoginResult {
    data class GoogleLoginIntentResult(val intent: Intent) : LoginResult()
    data class GoogleLoginSuccessResult(val googleSignInAccount: GoogleSignInAccount) : LoginResult()
    object EmailLoginSuccessResult : LoginResult()
    object CommunityLoginSuccessResult : LoginResult()
}