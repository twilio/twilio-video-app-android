package com.twilio.video.app.ui.room

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room

sealed class RoomEvent {
    data class Connecting(val room: Room) : RoomEvent()
    data class Connected(val room: Room) : RoomEvent()
    data class Disconnected(val room: Room) : RoomEvent()
    data class ConnectFailure(val room: Room) : RoomEvent()
    data class ParticipantConnected(val room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent()
    data class ParticipantDisconnected(val room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent()
    data class DominantSpeakerChanged(val room: Room, val remoteParticipant: RemoteParticipant?) : RoomEvent()
}
