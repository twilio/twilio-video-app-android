package com.twilio.video.app.participant

import com.twilio.video.LocalVideoTrack
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN
import com.twilio.video.Participant
import com.twilio.video.app.sdk.VideoTrackViewState

data class ParticipantViewState(
    val sid: String,
    val identity: String,
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

fun buildLocalParticipantViewState(
    localParticipant: Participant,
    identity: String,
    videoTrack: LocalVideoTrack? = null
) =
        ParticipantViewState(
                localParticipant.sid,
                identity,
                videoTrack?.let { VideoTrackViewState(it) },
                isLocalParticipant = true,
                networkQualityLevel = localParticipant.networkQualityLevel
        )