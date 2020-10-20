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
    object MaxParticipantFailure : RoomEvent()
    data class TokenError(val serviceError: AuthServiceError? = null) : RoomEvent()
    data class DominantSpeakerChanged(val newDominantSpeakerSid: String?) : RoomEvent()

    sealed class RemoteParticipantEvent : RoomEvent() {

        data class RemoteParticipantConnected(val participant: Participant) : RemoteParticipantEvent()
        data class VideoTrackUpdated(val sid: String, val videoTrack: VideoTrack?) : RemoteParticipantEvent()
        data class TrackSwitchOff(val sid: String, val videoTrack: VideoTrack, val switchOff: Boolean) : RemoteParticipantEvent()
        data class ScreenTrackUpdated(
            val sid: String,
            val screenTrack: VideoTrack?
        ) : RemoteParticipantEvent()
        data class MuteRemoteParticipant(val sid: String, val mute: Boolean) : RemoteParticipantEvent()
        data class NetworkQualityLevelChange(
            val sid: String,
            val networkQualityLevel: NetworkQualityLevel
        ) : RemoteParticipantEvent()
        data class RemoteParticipantDisconnected(val sid: String) : RemoteParticipantEvent()
    }

    sealed class LocalParticipantEvent: RoomEvent() {
        data class VideoTrackUpdated(val videoTrack: VideoTrack?): LocalParticipantEvent()
    }
}
