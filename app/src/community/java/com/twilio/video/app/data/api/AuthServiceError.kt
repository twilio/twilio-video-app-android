package com.twilio.video.app.data.api

enum class AuthServiceError {
    INVALID_PASSCODE_ERROR,
    EXPIRED_PASSCODE_ERROR;

    companion object {
        fun value(value: String): AuthServiceError? =
                when (value) {
                    "passcode incorrect" -> INVALID_PASSCODE_ERROR
                    "passcode expired" -> EXPIRED_PASSCODE_ERROR
                    else -> null
                }
    }
}
