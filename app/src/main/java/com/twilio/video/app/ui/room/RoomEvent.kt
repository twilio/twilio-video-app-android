package com.twilio.video.app.ui.room

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.app.data.api.AuthServiceError

sealed class RoomEvent {

    abstract val room: Room?

    object Connecting : RoomEvent() { override val room: Room? = null }
    data class TokenError(override val room: Room? = null, val serviceError: AuthServiceError? = null) : RoomEvent()
    data class RoomState(override val room: Room) : RoomEvent()
    data class ConnectFailure(override val room: Room) : RoomEvent()
    data class ParticipantConnected(override val room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent()
    data class NewRemoteVideoTrack(override val room: Room? = null, val remoteParticipant: RemoteParticipant) : RoomEvent()
    data class ParticipantDisconnected(override val room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent()
    data class DominantSpeakerChanged(override val room: Room, val remoteParticipant: RemoteParticipant?) : RoomEvent()
}
