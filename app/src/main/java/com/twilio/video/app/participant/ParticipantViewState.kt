package com.twilio.video.app.participant

import com.twilio.video.LocalVideoTrack
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN
import com.twilio.video.Participant
import com.twilio.video.VideoTrack

data class ParticipantViewState(
    val sid: String,
    val identity: String,
    val videoTrack: VideoTrack? = null,
    val screenTrack: VideoTrack? = null,
    val isMuted: Boolean = false,
    val isMirrored: Boolean = false,
    val isPinned: Boolean = false,
    val isDominantSpeaker: Boolean = false,
    val isLocalParticipant: Boolean = false,
    val networkQualityLevel: NetworkQualityLevel = NETWORK_QUALITY_LEVEL_UNKNOWN
)

fun buildParticipantViewState(participant: Participant) =
    ParticipantViewState(
            participant.sid,
            participant.identity,
            participant.videoTracks.firstOrNull()?.videoTrack,
            networkQualityLevel = participant.networkQualityLevel,
            isMuted = participant.audioTracks.firstOrNull() == null
    )

fun buildLocalParticipantViewState(
    localParticipant: Participant,
    identity: String,
    videoTrack: LocalVideoTrack? = null
) =
        ParticipantViewState(
                localParticipant.sid,
                identity,
                videoTrack,
                isLocalParticipant = true,
                networkQualityLevel = localParticipant.networkQualityLevel
        )