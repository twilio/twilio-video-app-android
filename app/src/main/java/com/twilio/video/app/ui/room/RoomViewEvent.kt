package com.twilio.video.app.ui.room

import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.video.LocalVideoTrack

sealed class RoomViewEvent {
    data class SelectAudioDevice(val device: AudioDevice) : RoomViewEvent()
    object ActivateAudioDevice : RoomViewEvent()
    object DeactivateAudioDevice : RoomViewEvent()
    data class Connect(
        val identity: String,
        val roomName: String,
        val isNetworkQualityEnabled: Boolean
    ) : RoomViewEvent()
    data class LocalVideoTrackPublished(val sid: String, val localVideoTrack: LocalVideoTrack) : RoomViewEvent()
    data class PinParticipant(val sid: String) : RoomViewEvent()
    object Disconnect : RoomViewEvent()
}
