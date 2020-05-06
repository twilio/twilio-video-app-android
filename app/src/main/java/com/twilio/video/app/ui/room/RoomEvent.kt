package com.twilio.video.app.ui.room

import com.twilio.video.Room
import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.participant.ParticipantViewState

sealed class RoomEvent(
    val room: Room? = null,
    val participantViewState: ParticipantViewState? = null
) {

    object Connecting : RoomEvent()
    class TokenError(val serviceError: AuthServiceError? = null) : RoomEvent()
    class RoomState(room: Room) : RoomEvent(room)
    class ConnectFailure(room: Room) : RoomEvent(room)
    class ParticipantConnected(room: Room, participantViewState: ParticipantViewState) : RoomEvent(room, participantViewState)
    class NewRemoteVideoTrack(participantViewState: ParticipantViewState) : RoomEvent(participantViewState = participantViewState)
    class ParticipantDisconnected(room: Room, participantViewState: ParticipantViewState) : RoomEvent(room, participantViewState)
    class DominantSpeakerChanged(room: Room, participantViewState: ParticipantViewState) : RoomEvent(room, participantViewState)
}
