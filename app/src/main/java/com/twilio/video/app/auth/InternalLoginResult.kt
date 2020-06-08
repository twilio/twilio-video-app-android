package com.twilio.video.app.auth

import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class InternalLoginResult : LoginResult {
    data class GoogleLoginIntentResult(val intent: Intent) : InternalLoginResult()
    data class GoogleLoginSuccessResult(val googleSignInAccount: GoogleSignInAccount) : InternalLoginResult()
    data class EmailLoginSuccessResult(val email: String) : InternalLoginResult()
}
