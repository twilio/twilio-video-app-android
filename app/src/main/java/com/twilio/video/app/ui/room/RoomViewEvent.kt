package com.twilio.video.app.ui.room

import android.content.Intent

sealed class RoomViewEvent {
    object ToggleLocalAudio : RoomViewEvent()
    object ToggleSpeakerPhone : RoomViewEvent()
    object StartScreenCapture : RoomViewEvent()
    object StopScreenCapture : RoomViewEvent()
    object DisconnectFromRoom : RoomViewEvent()
    object TearDownLocalMedia : RoomViewEvent()
    // TODO Remove activity dependency
    data class SetupLocalMedia(val volumeControlStream: Int) : RoomViewEvent()
    data class ConnectToRoom(val roomName: String, val tokenIdentity: String) : RoomViewEvent()
    data class SetupScreenCapture(val data: Intent) : RoomViewEvent()
}
