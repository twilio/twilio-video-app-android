package com.twilio.video.app.auth

import io.reactivex.Observable

class FirebaseAuthenticator(
    private val firebaseWrapper: FirebaseWrapper,
    private val googleAuthenticator: GoogleAuthenticator,
    private val emailAuthenticator: EmailAuthenticator
) : Authenticator {

    override fun login(loginEventObservable: Observable<LoginEvent>) =
            // TODO Figure out a better way to only subscribe to one authenticator at a time
            googleAuthenticator.login(loginEventObservable).mergeWith(emailAuthenticator.login(loginEventObservable))

    override fun loggedIn() = firebaseWrapper.instance.currentUser != null

    override fun logout() {
        googleAuthenticator.logout()
        emailAuthenticator.logout()
    }
}