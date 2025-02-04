package com.twilio.video.app.data.api

import com.google.gson.annotations.SerializedName

data class AuthServiceRequestDTO(
    @SerializedName("passcode") val passcode: String? = null,
    @SerializedName("user_identity") val user_identity: String? = null,
    @SerializedName("room_name") val room_name: String? = null,
    @SerializedName("create_room") val create_room: Boolean = false,
)
