package com.twilio.video.app.ui.room

import com.twilio.audioswitch.AudioDevice
import com.twilio.video.app.participant.ParticipantViewState
import io.uniflow.core.flow.data.UIState

data class RoomViewState(
    val title: String? = null,
    val participantThumbnails: List<ParticipantViewState>? = null,
    val primaryParticipant: ParticipantViewState? = null,
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice>? = null,
    val isLobbyLayoutVisible: Boolean = true,
    val isConnectingLayoutVisible: Boolean = false,
    val isConnectedLayoutVisible: Boolean = false,
    val isCameraEnabled: Boolean = false,
    val isMicEnabled: Boolean = false,
    val isAudioMuted: Boolean = true,
    val isVideoMuted: Boolean = true
) : UIState()
