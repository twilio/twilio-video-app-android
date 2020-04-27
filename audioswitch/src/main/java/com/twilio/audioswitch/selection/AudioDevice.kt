package com.twilio.audioswitch.selection

/**
 * This class represents a single audio device that has been retrieved by the [AudioDeviceSelector].
 * @property name the friendly name of the device
 */
sealed class AudioDevice {

    abstract val name: String

    /**
     * An [AudioDevice] representing a Bluetooth Headset.
     */
    data class BluetoothHeadset internal constructor(override val name: String) : AudioDevice()

    /**
     * An [AudioDevice] representing a Wired Headset.
     */
    object WiredHeadset : AudioDevice() { override val name: String = "Wired Headset" }

    /**
     * An [AudioDevice] representing the Earpiece.
     */
    object Earpiece : AudioDevice() { override val name: String = "Earpiece" }

    /**
     * An [AudioDevice] representing the Speakerphone.
     */
    object Speakerphone : AudioDevice() { override val name: String = "Speakerphone" }
}