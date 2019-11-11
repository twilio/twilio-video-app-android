package com.twilio.video.app.auth

class Authenticators(private val authenticators: List<Authenticator>) : Authenticator {

    override fun loggedIn() = authenticators.all { it.loggedIn() }

    override fun logout() {
        authenticators.forEach{ it.logout() }
    }

}