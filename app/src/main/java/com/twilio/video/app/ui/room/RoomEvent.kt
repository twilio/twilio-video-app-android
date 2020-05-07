package com.twilio.video.app.ui.room

import com.twilio.video.Room
import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.participant.ParticipantViewState

sealed class RoomEvent {

    object Connecting : RoomEvent()
    data class Connected(
        val remoteParticipants: List<ParticipantViewState>,
        val room: Room,
        val roomName: String
    ) : RoomEvent()
    object Disconnected : RoomEvent()
    object ConnectFailure : RoomEvent()
    data class TokenError(val serviceError: AuthServiceError? = null) : RoomEvent()
    data class ParticipantConnected(val participant: ParticipantViewState) : RoomEvent()
    data class ParticipantDisconnected(val participant: ParticipantViewState) : RoomEvent()
    data class AddRemoteVideoTrack(val participant: ParticipantViewState) : RoomEvent()
    data class DominantSpeakerChanged(val participant: ParticipantViewState) : RoomEvent()
}
