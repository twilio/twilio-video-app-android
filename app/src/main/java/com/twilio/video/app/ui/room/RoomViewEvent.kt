package com.twilio.video.app.ui.room

import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.video.app.participant.ParticipantViewState

sealed class RoomViewEvent {
    data class SelectAudioDevice(val device: AudioDevice) : RoomViewEvent()
    object ActivateAudioDevice : RoomViewEvent()
    object DeactivateAudioDevice : RoomViewEvent()
    data class Connect(
        val identity: String,
        val roomName: String,
        val isNetworkQualityEnabled: Boolean
    ) : RoomViewEvent()
    data class LocalVideoTrackPublished(val participantViewState: ParticipantViewState) : RoomViewEvent()
    object Disconnect : RoomViewEvent()
}
