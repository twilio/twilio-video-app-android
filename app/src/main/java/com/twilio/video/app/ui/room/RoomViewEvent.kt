package com.twilio.video.app.ui.room

import com.twilio.audio_manager.AudioDevice

sealed class RoomViewEvent {
    object Disconnect : RoomViewEvent()
    data class SelectAudioDevice(val device: AudioDevice) : RoomViewEvent()
    data class Connect(
        val identity: String,
        val roomName: String,
        val isNetworkQualityEnabled: Boolean
    ) : RoomViewEvent()
}
