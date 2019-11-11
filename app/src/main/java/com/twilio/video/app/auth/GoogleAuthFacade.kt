package com.twilio.video.app.auth

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.twilio.video.app.R
import com.twilio.video.app.util.AuthHelper
import com.twilio.video.app.util.AuthHelper.ERROR_AUTHENTICATION_FAILED
import com.twilio.video.app.util.AuthHelper.ERROR_UNAUTHORIZED_EMAIL

class GoogleAuthFacade {

    fun buildGoogleAPIClient(
            activity: FragmentActivity): GoogleSignInClient {
        return GoogleSignIn.getClient(activity, buildGoogleSignInOptions(activity))
    }

    fun signInWithGoogle(
            account: GoogleSignInAccount,
            activity: FragmentActivity,
            errorListener: AuthHelper.ErrorListener) {
        if (account.email!!.endsWith("@twilio.com")) {
            val mAuth = FirebaseAuth.getInstance()
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(
                            activity
                    ) { task ->
                        if (!task.isSuccessful) {
                            errorListener.onError(ERROR_AUTHENTICATION_FAILED)
                        }
                    }
        } else {
            errorListener.onError(ERROR_UNAUTHORIZED_EMAIL)
        }
    }

    private fun buildGoogleSignInOptions(activity: FragmentActivity): GoogleSignInOptions {
        val context = activity.baseContext
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .setHostedDomain("twilio.com")
                .build()
    }

    fun getSignInResultFromIntent(data: Intent) = Auth.GoogleSignInApi.getSignInResultFromIntent(data)

    fun signOut(googleSignInClient: GoogleSignInClient) {
        googleSignInClient.signOut()
    }
}
