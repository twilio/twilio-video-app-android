package com.twilio.video.app.videosdk

import com.twilio.video.AudioTrack
import com.twilio.video.Room
import com.twilio.video.VideoTrack

data class RoomViewState(
    val connectionState: Room.State? = null,
    val isConnecting: Boolean = false,
    val isConnectFailure: Boolean = false,
    val isLocalAudioMuted: Boolean = false,
    val isSpeakerPhoneMuted: Boolean = false,
    val isScreenShared: Boolean = false,
    val volumeControl: Boolean = false,
    val volumeControlStream: Int = 0,
    val room: Room? = null,
    val primaryParticipant: ParticipantViewState? = null,
    val participants: List<ParticipantViewState>? = null
)

data class ParticipantViewState(
    val sid: String,
    val identity: String,
    val videoTrack: VideoTrack? = null,
    val audioTrack: AudioTrack? = null,
    val isMuted: Boolean = false,
    val isMirrored: Boolean = true,
    val showNetworkQualityLevel: Boolean = true,
    val isDominantSpeaker: Boolean = false
)
