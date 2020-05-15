package com.twilio.video.app.ui.room

import com.twilio.video.Room
import com.twilio.video.app.data.api.AuthServiceError

sealed class RoomViewEffect {

    // TODO Remove duplicated RoomEvents once all SDK code is decoupled from RoomActivity
    object Connecting : RoomViewEffect()
    data class Connected(val room: Room) : RoomViewEffect()
    object Disconnected : RoomViewEffect()

    object ShowConnectFailureDialog : RoomViewEffect()
    data class ShowTokenErrorDialog(val serviceError: AuthServiceError? = null ) : RoomViewEffect()
}