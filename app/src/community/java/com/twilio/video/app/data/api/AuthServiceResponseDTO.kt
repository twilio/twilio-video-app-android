package com.twilio.video.app.data.api

import com.google.gson.annotations.SerializedName

data class AuthServiceResponseDTO(
    val token: String? = null,
    @SerializedName("room_type") val roomType: RoomType? = null
)
