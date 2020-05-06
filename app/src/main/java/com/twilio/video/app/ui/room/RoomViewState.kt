package com.twilio.video.app.ui.room

import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.video.app.participant.ParticipantViewState

data class RoomViewState(
    val roomState: RoomState? = null,
    val roomName: String? = null,
    val participantThumbnails: List<ParticipantViewState>? = null,
    val primaryParticipant: ParticipantViewState? = null,
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice>? = null
)