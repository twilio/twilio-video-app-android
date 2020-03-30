package com.twilio.video.app.ui.room

import com.twilio.audio_manager.AudioDevice

data class AudioViewState(
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice> = emptyList()
)