package com.twilio.video.app.auth

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.twilio.video.app.R
import com.twilio.video.app.util.AuthHelper
import com.twilio.video.app.util.AuthHelper.ERROR_AUTHENTICATION_FAILED
import com.twilio.video.app.util.AuthHelper.ERROR_UNAUTHORIZED_EMAIL

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class GoogleAuthenticator (
        private val firebaseWrapper: FirebaseWrapper,
        context: Context,
        private val googleAuthWrapper: GoogleAuthWrapper,
        private val googleSignInWrapper: GoogleSignInWrapper,
        private val googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper
    ) : Authenticator {

    private val googleSignInClient: GoogleSignInClient = buildGoogleSignInClient(context)

    override fun loggedIn() = firebaseWrapper.instance.currentUser != null

    override fun logout() {
        googleSignInClient.signOut()
    }

    fun login(
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

    fun getSignInIntent() = googleSignInClient.signInIntent

    fun getSignInResultFromIntent(data: Intent): GoogleSignInResult? = googleAuthWrapper.getSignInResultFromIntent(data)

    private fun buildGoogleSignInClient(context: Context) =
            googleSignInWrapper.getClient(context, buildGoogleSignInOptions(context))

    private fun buildGoogleSignInOptions(context: Context): GoogleSignInOptions {
        return googleSignInOptionsBuilderWrapper.run {
            requestIdToken(context.getString(R.string.default_web_client_id))
            requestEmail()
            setHostedDomain("twilio.com")
            build()
        }
    }
}
