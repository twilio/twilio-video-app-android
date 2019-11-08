package com.twilio.video.app.auth

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.twilio.video.app.R

class GoogleAuthenticator {

    fun buildGoogleAPIClient(
            activity: FragmentActivity): GoogleSignInClient {
        return GoogleSignIn.getClient(activity, buildGoogleSignInOptions(activity))
    }

    private fun buildGoogleSignInOptions(activity: FragmentActivity): GoogleSignInOptions {
        val context = activity.baseContext
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .setHostedDomain("twilio.com")
                .build()
    }

}
