package com.twilio.audioswitch

/** A listener that receives the most recently available [AudioDevice]s.
 * Receives a list of the most recently available [AudioDevice]s. Also provides the
 * currently selected [AudioDevice] from [AudioDeviceSelector].
 *
 * @param audioDevices the list of [AudioDevice]s or null if none are available.
 * @param selectedAudioDevice the currently selected device or null if none have been
 * selected.
 */
typealias AudioDeviceChangeListener = (audioDevices: List<AudioDevice>?, selectedAudioDevice: AudioDevice?) -> Unit