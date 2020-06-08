package com.twilio.audioswitch.selection

/** A listener that receives the most recently available [AudioDevice]s.
 * Receives a list of the most recently available [AudioDevice]s. Also provides the
 * currently selected [AudioDevice] from [AudioDeviceSelector].
 *
 * @param audioDevices the list of [AudioDevice]s or an empty list if none are available.
 * @param selectedAudioDevice the currently selected device or null if no device has been selected.
 */
typealias AudioDeviceChangeListener = (
    audioDevices: List<AudioDevice>,
    selectedAudioDevice: AudioDevice?
) -> Unit
