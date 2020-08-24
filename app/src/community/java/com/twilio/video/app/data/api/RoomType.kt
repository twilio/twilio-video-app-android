package com.twilio.video.app.data.api

import com.google.gson.annotations.SerializedName

enum class RoomType {
    @SerializedName("group") GROUP,
    @SerializedName("group-small") GROUP_SMALL,
    @SerializedName("peer-to-peer") PEER_TO_PEER
}
