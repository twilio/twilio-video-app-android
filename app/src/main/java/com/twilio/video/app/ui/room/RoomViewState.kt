package com.twilio.video.app.ui.room

import com.twilio.audioswitch.AudioDevice
import com.twilio.video.app.participant.ParticipantViewState
import com.twilio.video.app.sdk.VideoTrackViewState
import com.twilio.video.app.ui.room.LayoutState.Lobby
import io.uniflow.core.flow.data.UIState

data class RoomViewState(
    val primaryParticipant: ParticipantViewState,
    val title: String? = null,
    val participantThumbnails: List<ParticipantViewState>? = null,
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice>? = null,
    val layoutState: LayoutState = Lobby,
    val isCameraEnabled: Boolean = false,
    val localVideoTrack: VideoTrackViewState? = null,
    val isMicEnabled: Boolean = false,
    val isAudioMuted: Boolean = true,
    val isVideoOff: Boolean = false,
    val isScreenCaptureOn: Boolean = false
) : UIState()

sealed class LayoutState {
    object Connecting : LayoutState()
    object Connected : LayoutState()
    object Lobby : LayoutState()
}
