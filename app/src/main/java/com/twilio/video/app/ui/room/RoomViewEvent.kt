package com.twilio.video.app.ui.room

import android.content.Intent
import com.twilio.audioswitch.AudioDevice
import com.twilio.video.app.sdk.VideoTrackViewState

sealed class RoomViewEvent {
    object OnResume: RoomViewEvent()
    object OnPause: RoomViewEvent()
    object ToggleLocalVideo: RoomViewEvent()
    object ToggleLocalAudio: RoomViewEvent()
    data class StartScreenCapture(val captureResultCode: Int, val captureIntent: Intent): RoomViewEvent()
    object StopScreenCapture: RoomViewEvent()
    object SwitchCamera: RoomViewEvent()
    data class SelectAudioDevice(val device: AudioDevice): RoomViewEvent()
    object ActivateAudioDevice: RoomViewEvent()
    object DeactivateAudioDevice: RoomViewEvent()
    data class Connect(val identity: String, val roomName: String): RoomViewEvent()
    data class PinParticipant(val sid: String): RoomViewEvent()
    data class VideoTrackRemoved(val sid: String): RoomViewEvent()
    data class ScreenTrackRemoved(val sid: String): RoomViewEvent()
    object Disconnect: RoomViewEvent()
}
