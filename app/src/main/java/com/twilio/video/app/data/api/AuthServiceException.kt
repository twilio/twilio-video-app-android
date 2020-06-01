package com.twilio.video.app.data.api

class AuthServiceException(
    throwable: Throwable? = null,
    val error: AuthServiceError? = null,
    message: String? = null
) : RuntimeException(message, throwable)
