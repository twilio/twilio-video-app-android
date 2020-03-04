package com.twilio.video.app.data.api

data class AuthServiceParameters(
    val passcode: String,
    val userIdentity: String? = null,
    val roomName: String? = null
) : TokenServiceParameters