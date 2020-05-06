package com.twilio.video.app.ui.room

import com.twilio.video.app.data.api.AuthServiceError
import com.twilio.video.app.participant.ParticipantViewState

sealed class RoomState {

    object Connecting : RoomState()
    data class Connected(val remoteParticipants: List<ParticipantViewState>, val roomName: String? = null) : RoomState()
    object Disconnected : RoomState()
    object ConnectFailure : RoomState()
    data class TokenError(val serviceError: AuthServiceError? = null) : RoomState()
    data class ParticipantConnected(val participant: ParticipantViewState) : RoomState()
    data class ParticipantDisconnected(val participant: ParticipantViewState) : RoomState()
    data class NewRemoteVideoTrack(val participant: ParticipantViewState) : RoomState()
    data class DominantSpeakerChanged(val participant: ParticipantViewState) : RoomState()
}
