package com.twilio.video.app.data.api

data class AuthServiceRequestDTO(
    val passcode: String?,
    val user_identity: String?,
    val room_name: String?
)
