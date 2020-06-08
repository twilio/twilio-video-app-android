package com.twilio.audioswitch.android

import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE

internal class FakeBluetoothDevice(
    override val name: String = "Fake Bluetooth",
    override val deviceClass: Int? = AUDIO_VIDEO_HANDSFREE
) : BluetoothDeviceWrapper
