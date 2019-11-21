package com.twilio.video.app.auth

import android.content.Intent
import com.twilio.video.app.auth.LoginEvent.EmailLogin
import com.twilio.video.app.auth.LoginEvent.GoogleLogin
import com.twilio.video.app.auth.LoginEvent.TokenLogin
import com.twilio.video.app.auth.LoginIntentEvent.GoogleSignInIntent
import io.reactivex.Maybe
import io.reactivex.Single
import java.lang.RuntimeException

class FirebaseFacade(
        private val firebaseWrapper: FirebaseWrapper,
        private val googleAuthenticator: GoogleAuthenticator,
        private val emailAuthenticator: EmailAuthenticator
) : Authenticator {

    override fun login(event: Single<LoginEvent>) : Maybe<Authenticator.LoginError> {
//        return when (event) {
//            is GoogleLogin -> {
//                googleAuthenticator.login(event)
//            }
//            is EmailLogin -> {
//                emailAuthenticator.login(event)
//            }
//            is TokenLogin -> throw RuntimeException("Illegal login event processed")
//        }

        return Maybe.create({

        })
    }

    fun getLoginIntent(event: LoginIntentEvent): Intent {
        return when (event) {
            GoogleSignInIntent -> {
                googleAuthenticator.getSignInIntent()
            }
        }
    }

    override fun loggedIn() = firebaseWrapper.instance.currentUser != null

    override fun logout() {
        googleAuthenticator.logout()
        emailAuthenticator.logout()
    }

}