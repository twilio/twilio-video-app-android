package com.twilio.video.app.data.api

import com.twilio.video.app.data.api.model.RoomProperties

data class VideoServiceParameters(
    val identity: String,
    val roomProperties: RoomProperties
) : TokenServiceParameters