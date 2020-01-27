package com.twilio.video.app.auth

import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleSignInOptionsBuilderWrapper(defaultSignIn: GoogleSignInOptions) {

    private val builder: GoogleSignInOptions.Builder = GoogleSignInOptions.Builder(defaultSignIn)

    fun requestIdToken(token: String) {
        builder.requestIdToken(token)
    }

    fun requestEmail() {
        builder.requestEmail()
    }

    fun setHostedDomain(hostedDomain: String) {
        builder.setHostedDomain(hostedDomain)
    }

    fun build() = GoogleSignInOptionsWrapper(builder.build())
}
