package com.twilio.video.app.data.api

class AuthServiceException(
    throwable: Throwable,
    val error: AuthServiceError? = null
) : RuntimeException(throwable)