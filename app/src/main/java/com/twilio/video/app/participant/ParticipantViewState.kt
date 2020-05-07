package com.twilio.video.app.participant

import com.twilio.video.LocalParticipant
import com.twilio.video.LocalVideoTrack
import com.twilio.video.NetworkQualityLevel
import com.twilio.video.NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN
import com.twilio.video.RemoteParticipant
import com.twilio.video.VideoTrack

data class ParticipantViewState(
    val sid: String? = null,
    val identity: String? = null,
    val videoTrack: VideoTrack? = null,
    val muted: Boolean = false,
    val mirror: Boolean = false,
    val isPinned: Boolean = false,
    val isScreenSharing: Boolean = false,
    val isDominantSpeaker: Boolean = false,
    val isLocalParticipant: Boolean = false,
    val networkQualityLevel: NetworkQualityLevel = NETWORK_QUALITY_LEVEL_UNKNOWN
)

fun buildParticipantViewState(remoteParticipant: RemoteParticipant) =
    ParticipantViewState(
            remoteParticipant.sid,
            remoteParticipant.identity,
            remoteParticipant.remoteVideoTracks.firstOrNull()?.remoteVideoTrack,
            networkQualityLevel = remoteParticipant.networkQualityLevel
    )

fun buildLocalParticipantViewState(
    localParticipant: LocalParticipant,
    identity: String,
    videoTrack: LocalVideoTrack
) =
        ParticipantViewState(
                localParticipant.sid,
                identity,
                videoTrack,
                isLocalParticipant = true,
                networkQualityLevel = localParticipant.networkQualityLevel
        )