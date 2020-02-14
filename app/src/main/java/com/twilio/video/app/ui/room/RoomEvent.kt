package com.twilio.video.app.ui.room

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room

sealed class RoomEvent(val room: Room) {
    class Connecting(room: Room) : RoomEvent(room)
    class Connected(room: Room) : RoomEvent(room)
    class Disconnected(room: Room) : RoomEvent(room)
    class ConnectFailure(room: Room) : RoomEvent(room)
    class ParticipantConnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class ParticipantDisconnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class DominantSpeakerChanged(room: Room, val remoteParticipant: RemoteParticipant?) : RoomEvent(room)
}
