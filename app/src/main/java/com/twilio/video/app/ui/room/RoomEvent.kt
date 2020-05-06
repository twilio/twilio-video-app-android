package com.twilio.video.app.ui.room

import com.twilio.video.RemoteParticipant
import com.twilio.video.Room
import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.participant.ParticipantViewState

sealed class RoomEvent(val room: Room? = null) {

    object Connecting : RoomEvent()
    class TokenError(val serviceError: AuthServiceError? = null) : RoomEvent()
    class RoomState(room: Room) : RoomEvent(room)
    class ConnectFailure(room: Room) : RoomEvent(room)
    class ParticipantConnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class NewRemoteVideoTrack(val participantViewState: ParticipantViewState) : RoomEvent()
    class ParticipantDisconnected(room: Room, val remoteParticipant: RemoteParticipant) : RoomEvent(room)
    class DominantSpeakerChanged(room: Room, val remoteParticipant: RemoteParticipant?) : RoomEvent(room)
}
