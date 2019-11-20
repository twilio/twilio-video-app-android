package com.twilio.video.app.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.twilio.video.app.R
import com.twilio.video.app.auth.Authenticator.MissingEmailError
import com.twilio.video.app.auth.LoginEvent.GoogleLogin
import io.reactivex.Maybe
import io.reactivex.MaybeEmitter

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class GoogleAuthenticator(
        private val firebaseWrapper: FirebaseWrapper,
        context: Context,
        private val googleAuthWrapper: GoogleAuthWrapper,
        private val googleSignInWrapper: GoogleSignInWrapper,
        private val googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper,
        private val googleAuthProviderWrapper: GoogleAuthProviderWrapper
) : Authenticator {

    private val googleSignInClient: GoogleSignInClient = buildGoogleSignInClient(context)

    override fun login(googleLogin: LoginEvent): Maybe<Authenticator.LoginError> {
        require(googleLogin is GoogleLogin) { "Expecting ${GoogleLogin::class.simpleName} as LoginEvent" }
        return Maybe.create { maybe ->
            getSignInResultFromIntent(googleLogin.signInResultIntent)?.let { result ->
                if (result.isSuccess) {
                    result.signInAccount?.let { account ->
                        loginWithAccount(account, maybe)
                    } ?: maybe.onSuccess(NullGoogleSignInAccountError)
                } else {
                    maybe.onSuccess(LoginIntentError)
                }
                // TODO: failed to sign in with google
            }
        }
    }

    override fun logout() {
        googleSignInClient.signOut()
    }

    private fun loginWithAccount(account: GoogleSignInAccount, maybe: MaybeEmitter<Authenticator.LoginError>) {
        val email = account.email
        val idToken = account.idToken
        if (email.isNullOrBlank()) {
            maybe.onSuccess(MissingEmailError)
            return
        }
        if (email.endsWith("@twilio.com")) {
            maybe.onSuccess(WrongEmailDomainError)
            return
        }
        if (idToken.isNullOrBlank()) {
            maybe.onSuccess(MissingIdTokenError)
            return
        }
        val credential = googleAuthProviderWrapper.getCredential(idToken)
        firebaseWrapper.instance.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        maybe.onComplete()
                    } else {
                        maybe.onSuccess(GoogleAuthError)
                    }
                }
    }

    fun getSignInIntent() = googleSignInClient.signInIntent

    private fun getSignInResultFromIntent(data: Intent): GoogleSignInResult? = googleAuthWrapper.getSignInResultFromIntent(data)

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

object LoginIntentError : Authenticator.LoginError()
object WrongEmailDomainError : Authenticator.LoginError()
object GoogleAuthError : Authenticator.LoginError()
object NullGoogleSignInAccountError : Authenticator.LoginError()
object MissingIdTokenError : Authenticator.LoginError()
