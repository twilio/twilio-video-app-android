package com.twilio.video.app.ui.room

import com.twilio.video.NetworkQualityLevel
import com.twilio.video.Participant
import com.twilio.video.Room
import com.twilio.video.VideoTrack
import com.twilio.video.app.data.api.AuthServiceError

sealed class RoomEvent {

    object Connecting : RoomEvent()
    data class Connected(
        val participants: List<Participant>,
        val room: Room,
        val roomName: String
    ) : RoomEvent()
    object Disconnected : RoomEvent()
    object ConnectFailure : RoomEvent()
    data class TokenError(val serviceError: AuthServiceError? = null) : RoomEvent()
    data class DominantSpeakerChanged(val newDominantSpeakerSid: String?) : RoomEvent()

    sealed class ParticipantEvent : RoomEvent() {

        data class ParticipantConnected(val participant: Participant) : ParticipantEvent()
        data class VideoTrackUpdated(
            val sid: String,
            val videoTrack: VideoTrack?
        ) : ParticipantEvent()
        data class MuteParticipant(val sid: String, val mute: Boolean) : ParticipantEvent()
        data class NetworkQualityLevelChange(
            val sid: String,
            val networkQualityLevel: NetworkQualityLevel
        ) : ParticipantEvent()
        data class ParticipantDisconnected(val sid: String) : ParticipantEvent()
    }
}
