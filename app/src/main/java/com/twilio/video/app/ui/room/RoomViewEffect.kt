package com.twilio.video.app.ui.room

import com.twilio.video.app.data.api.AuthServiceError

sealed class RoomViewEffect {

    // TODO Remove duplicated RoomEvents once all SDK code is decoupled from RoomActivity
    object Connecting : RoomViewEffect()
    object Connected : RoomViewEffect()
    object Disconnected : RoomViewEffect()

    object ShowConnectFailureDialog : RoomViewEffect()
    data class ShowTokenErrorDialog(val serviceError: AuthServiceError? = null ) : RoomViewEffect()
}