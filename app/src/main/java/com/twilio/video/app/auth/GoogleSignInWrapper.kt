package com.twilio.video.app.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn

class GoogleSignInWrapper {

    fun getClient(context: Context, googleSignInOptionsWrapper: GoogleSignInOptionsWrapper) =
            GoogleSignIn.getClient(context, googleSignInOptionsWrapper.googleSignInOptions)
}
