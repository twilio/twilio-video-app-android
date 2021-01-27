package com.twilio.video.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInResult

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class GoogleAuthProvider(
    private val firebaseWrapper: FirebaseWrapper,
    context: Context,
    private val googleAuthWrapper: GoogleAuthWrapper,
    googleSignInWrapper: GoogleSignInWrapper,
    googleSignInOptionsWrapper: GoogleSignInOptionsWrapper,
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper,
    private val acceptedDomain: String? = null,
) {

    private val googleSignInClient: GoogleSignInClient =
            googleSignInWrapper.getClient(context, googleSignInOptionsWrapper)

    val signInIntent
        get() = googleSignInClient.signInIntent

    fun login(signInResultIntent: Intent) {
                getSignInResultFromIntent(signInResultIntent)?.let { result ->
                    if (result.isSuccess) {
                        result.signInAccount?.let { account ->
                            loginWithAccount(account)
                        } //?: onError(observable)
                    } else {
//                        onError(observable)
                    }
                }
            }

    private fun getSignInResultFromIntent(data: Intent): GoogleSignInResult? = googleAuthWrapper.getSignInResultFromIntent(data)

    fun logout() {
        googleSignInClient.signOut()
    }

    private fun loginWithAccount(account: GoogleSignInAccount) {
        val email = account.email
        val idToken = account.idToken
        if (email.isNullOrBlank()) {
//            onError(observable)
            return
        }
        acceptedDomain?.let {
            if (!email.endsWith(it)) {
//                onError(observable)
                return
            }
        }
        if (idToken.isNullOrBlank()) {
//            onError(observable)
            return
        }
        val credential = googleAuthProviderWrapper.getCredential(idToken)
        firebaseWrapper.instance.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
//                        observable.onNext(GoogleLoginSuccessResult(account))
//                        observable.onComplete()
//                        disposables.clear()
                    } else {
//                        onError(observable)
                    }
                }
    }
}
