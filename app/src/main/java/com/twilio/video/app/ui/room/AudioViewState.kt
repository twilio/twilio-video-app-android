package com.twilio.video.app.ui.room

import com.twilio.audio_router.AudioDevice

data class AudioViewState(
    val selectedDevice: AudioDevice? = null,
    val availableAudioDevices: List<AudioDevice> = emptyList()
)