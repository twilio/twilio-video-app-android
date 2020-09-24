package com.twilio.video.app.data.api

import com.google.gson.annotations.SerializedName
import com.twilio.video.app.data.api.model.Topology

data class AuthServiceResponseDTO(
    val token: String? = null,
    @SerializedName("room_type") val topology: Topology? = null
)
