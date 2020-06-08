package com.twilio.video.app.auth

import io.reactivex.Observable

class FirebaseAuthenticator(
    private val firebaseWrapper: FirebaseWrapper,
    private val authenticationProviders: List<AuthenticationProvider>
) : Authenticator {

    override fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult> {
        // TODO Figure out a better way to only subscribe to one authenticator at a time
        val observables: MutableList<Observable<LoginResult>> = mutableListOf()
        authenticationProviders.forEach {
            observables.add(it.login(loginEventObservable))
        }
        return Observable.merge(observables)
    }

    override fun login(loginEvent: LoginEvent): Observable<LoginResult> {
        return login(Observable.just(loginEvent))
    }

    override fun loggedIn() = firebaseWrapper.instance.currentUser != null

    override fun logout() {
        authenticationProviders.forEach { it.logout() }
    }
}
