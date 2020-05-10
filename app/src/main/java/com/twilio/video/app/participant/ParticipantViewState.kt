package com.twilio.video.app.participant

import com.twilio.video.LocalParticipant
import com.twilio.video.LocalVideoTrack
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN
import com.twilio.video.Participant
import com.twilio.video.VideoTrack
import com.twilio.video.app.sdk.getFirstVideoTrack

data class ParticipantViewState(
    val sid: String,
    val identity: String,
    val videoTrack: VideoTrack? = null,
    val isMuted: Boolean = false,
    val isMirrored: Boolean = false,
    val isPinned: Boolean = false,
    val isScreenSharing: Boolean = false,
    val isDominantSpeaker: Boolean = false,
    val isLocalParticipant: Boolean = false,
    val networkQualityLevel: NetworkQualityLevel = NETWORK_QUALITY_LEVEL_UNKNOWN
)

fun buildParticipantViewState(remoteParticipant: Participant) =
    ParticipantViewState(
            remoteParticipant.sid,
            remoteParticipant.identity,
            remoteParticipant.getFirstVideoTrack(),
            networkQualityLevel = remoteParticipant.networkQualityLevel,
            isMuted = remoteParticipant.audioTracks.firstOrNull() == null
    )

fun buildLocalParticipantViewState(
    localParticipant: LocalParticipant,
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