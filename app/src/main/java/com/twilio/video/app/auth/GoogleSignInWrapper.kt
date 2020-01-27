package com.twilio.video.app.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class GoogleSignInWrapper {

    fun getClient(context: Context, googleSignInOptions: GoogleSignInOptionsWrapper) =
            GoogleSignIn.getClient(context, googleSignInOptions.googleSignInOptions)
}
