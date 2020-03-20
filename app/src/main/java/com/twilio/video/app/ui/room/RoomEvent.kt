package com.twilio.video.app.ui.room

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room

sealed class RoomEvent(val room: Room? = null) {
    object TokenError : RoomEvent()
    object Connecting : RoomEvent()
    class RoomState(room: Room) : RoomEvent(room)
    class ConnectFailure(room: Room) : RoomEvent(room)
    class ParticipantConnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class ParticipantDisconnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class DominantSpeakerChanged(room: Room, val remoteParticipant: RemoteParticipant?) : RoomEvent(room)
}
