package com.twilio.video.app.videosdk

import android.app.Activity
import android.content.Intent
import com.twilio.video.app.ui.room.ParticipantController

sealed class RoomViewEvent {
    object ToggleLocalAudio: RoomViewEvent()
    object ToggleSpeakerPhone: RoomViewEvent()
    object StartScreenCapture: RoomViewEvent()
    object StopScreenCapture: RoomViewEvent()
    object DisconnectFromRoom: RoomViewEvent()
    data class SetupLocalMedia(val activity: Activity, // TODO Remove activity dependency
                               val participantController: ParticipantController): RoomViewEvent()
    data class ConnectToRoom(val roomName: String, val tokenIdentity: String): RoomViewEvent()
    data class SetupScreenCapture(val data: Intent): RoomViewEvent()
}
