package com.twilio.audioswitch.selection

/**
 * This class represents a single audio device that has been retrieved by the [AudioDeviceSelector].
 * It contains information about the audio device.
 */
data class AudioDevice internal constructor(
    /** The type of audio device defined in [Type].  */
    val type: Type,
    /** The name of the audio device.  */
    val name: String
) {

    /** A type of audio device.  */
    enum class Type {
        SPEAKERPHONE, WIRED_HEADSET, EARPIECE, BLUETOOTH
    }
}