package com.twilio.video.app.ui.room

import com.twilio.audioswitch.selection.AudioDevice

sealed class RoomViewEvent {
    object ScreenLoad : RoomViewEvent()
    data class SelectAudioDevice(val device: AudioDevice) : RoomViewEvent()
    object ActivateAudioDevice : RoomViewEvent()
    object DeactivateAudioDevice : RoomViewEvent()
    data class Connect(val identity: String, val roomName: String) : RoomViewEvent()
    data class PinParticipant(val sid: String) : RoomViewEvent()
    data class ToggleLocalVideo(val sid: String) : RoomViewEvent()
    data class VideoTrackRemoved(val sid: String) : RoomViewEvent()
    data class ScreenTrackRemoved(val sid: String) : RoomViewEvent()
    object Disconnect : RoomViewEvent()
}
