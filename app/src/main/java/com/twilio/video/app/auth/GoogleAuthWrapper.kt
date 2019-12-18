package com.twilio.video.app.auth

import android.content.Intent
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInResult

class GoogleAuthWrapper {

    fun getSignInResultFromIntent(intent: Intent): GoogleSignInResult? = Auth.GoogleSignInApi.getSignInResultFromIntent(intent)
}
