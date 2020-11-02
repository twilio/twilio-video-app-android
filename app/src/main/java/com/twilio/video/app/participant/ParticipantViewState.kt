package com.twilio.video.app.participant

import com.twilio.video.NetworkQualityLevel
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN
import com.twilio.video.Participant
import com.twilio.video.RemoteVideoTrack
import com.twilio.video.app.sdk.VideoTrackViewState

data class ParticipantViewState(
    val sid: String? = null,
    val identity: String? = null,
    val videoTrack: VideoTrackViewState? = null,
    val screenTrack: VideoTrackViewState? = null,
    val isMuted: Boolean = false,
    val isMirrored: Boolean = false,
    val isPinned: Boolean = false,
    val isDominantSpeaker: Boolean = false,
    val isLocalParticipant: Boolean = false,
    val networkQualityLevel: NetworkQualityLevel = NETWORK_QUALITY_LEVEL_UNKNOWN
) {
    val isScreenSharing: Boolean get() = screenTrack != null

    fun getRemoteVideoTrack(): RemoteVideoTrack? =
            if (!isLocalParticipant) videoTrack?.videoTrack as RemoteVideoTrack? else null

    fun getRemoteScreenTrack(): RemoteVideoTrack? =
            if (!isLocalParticipant) screenTrack?.videoTrack as RemoteVideoTrack? else null
}

fun buildParticipantViewState(participant: Participant): ParticipantViewState {
    val videoTrack = participant.videoTracks.firstOrNull()?.videoTrack
    return ParticipantViewState(
            participant.sid,
            participant.identity,
            videoTrack?.let { VideoTrackViewState(it) },
            networkQualityLevel = participant.networkQualityLevel,
            isMuted = participant.audioTracks.firstOrNull() == null
    )
}
