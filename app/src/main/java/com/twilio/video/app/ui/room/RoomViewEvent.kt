package com.twilio.video.app.ui.room

import com.twilio.audio_manager.AudioDevice

sealed class RoomViewEvent {
    data class SelectAudioDevice(val device: AudioDevice) : RoomViewEvent()
    object ActivateAudioDevice : RoomViewEvent()
    object DeactivateAudioDevice : RoomViewEvent()
    data class Connect(
        val identity: String,
        val roomName: String,
        val isNetworkQualityEnabled: Boolean
    ) : RoomViewEvent()
    object Disconnect : RoomViewEvent()
}
