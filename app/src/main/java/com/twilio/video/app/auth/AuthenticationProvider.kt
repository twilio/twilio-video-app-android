package com.twilio.video.app.auth

import io.reactivex.Observable

interface AuthenticationProvider {

    fun login(loginEventObservable: Observable<LoginEvent>): Observable<LoginResult>

    fun logout()
}
