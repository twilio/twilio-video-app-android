package com.twilio.video.app.ui.room

import com.twilio.audioswitch.AudioDevice
import com.twilio.video.app.sdk.VideoTrackViewState

sealed class RoomViewEvent {
    object OnResume: RoomViewEvent()
    object OnPause: RoomViewEvent()
    object RefreshViewState : RoomViewEvent()
    object CheckPermissions : RoomViewEvent()
    data class SelectAudioDevice(val device: AudioDevice) : RoomViewEvent()
    object ActivateAudioDevice : RoomViewEvent()
    object DeactivateAudioDevice : RoomViewEvent()
    data class Connect(val identity: String, val roomName: String) : RoomViewEvent()
    data class PinParticipant(val sid: String) : RoomViewEvent()
    data class ToggleLocalVideo(val sid: String, val videoTrackViewState: VideoTrackViewState? = null) : RoomViewEvent()
    data class VideoTrackRemoved(val sid: String) : RoomViewEvent()
    data class ScreenTrackRemoved(val sid: String) : RoomViewEvent()
    object Disconnect : RoomViewEvent()
}
