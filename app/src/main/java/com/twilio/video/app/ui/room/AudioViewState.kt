package com.twilio.video.app.ui.room

import com.twilio.audioswitch.selection.AudioDevice

data class AudioViewState(
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice>? = null
)