package com.twilio.video.app.auth

// TODO unit test as part of https://issues.corp.twilio.com/browse/AHOYAPPS-140
class Authenticators(private val authenticators: List<Authenticator>) : Authenticator {

    override fun loggedIn() = authenticators.all { it.loggedIn() }

    override fun logout() = authenticators.forEach{ it.logout() }

}