package com.twilio.video.app.ui.room

import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.video.Room
import com.twilio.video.app.participant.ParticipantViewState

data class RoomViewState(
    val room: Room? = null, // TODO Remove all room references from UI layer
    val roomEvent: RoomEvent? = null,
    val roomName: String? = null,
    val participantThumbnails: List<ParticipantViewState>? = null,
    val primaryParticipant: ParticipantViewState? = null,
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice>? = null
)