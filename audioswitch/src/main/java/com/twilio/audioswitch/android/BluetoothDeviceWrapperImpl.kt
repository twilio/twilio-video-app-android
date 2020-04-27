package com.twilio.audioswitch.android

import android.bluetooth.BluetoothDevice

internal class BluetoothDeviceWrapperImpl(
    private val device: BluetoothDevice,
    override val name: String = device.name,
    override val deviceClass: Int? = device.bluetoothClass?.deviceClass
) : BluetoothDeviceWrapper