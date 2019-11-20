package com.twilio.video.app.auth

import android.content.Intent
import com.twilio.video.app.auth.LoginEvent.EmailLogin
import com.twilio.video.app.auth.LoginEvent.GoogleLogin
import com.twilio.video.app.auth.LoginIntentEvent.GoogleSignInIntent
import io.reactivex.Maybe

class FirebaseFacade(
        private val firebaseWrapper: FirebaseWrapper,
        private val googleAuthenticator: GoogleAuthenticator,
        private val emailAuthenticator: EmailAuthenticator
) {

    fun login(event: LoginEvent) : Maybe<Authenticator.LoginError> {
        return when (event) {
            is GoogleLogin -> {
                googleAuthenticator.login(event)
            }
            is EmailLogin -> {
                emailAuthenticator.login(event)
            }
        }
    }

    fun getLoginIntent(event: LoginIntentEvent): Intent {
        return when (event) {
            GoogleSignInIntent -> {
                googleAuthenticator.getSignInIntent()
            }
        }
    }

    fun loggedIn() = getCurrentUser() != null

    fun getCurrentUser() = firebaseWrapper.instance.currentUser

    fun logout() {
        googleAuthenticator.logout()
        emailAuthenticator.logout()
    }

}