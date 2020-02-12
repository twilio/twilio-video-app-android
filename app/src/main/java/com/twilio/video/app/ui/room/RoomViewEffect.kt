package com.twilio.video.app.ui.room

sealed class RoomViewEffect {
    object ScreenShareError : RoomViewEffect()
    object RequestScreenSharePermission : RoomViewEffect()
}