package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.android.BluetoothDeviceWrapper

internal interface BluetoothDeviceConnectionListener {
    fun onBluetoothConnected(bluetoothDeviceWrapper: BluetoothDeviceWrapper)
    fun onBluetoothDisconnected()
}
