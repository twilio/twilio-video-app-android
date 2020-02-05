package com.twilio.video.app.videosdk

sealed class RoomViewEffect {
    object ScreenShareError: RoomViewEffect()
    object RequestScreenSharePermission: RoomViewEffect()
}