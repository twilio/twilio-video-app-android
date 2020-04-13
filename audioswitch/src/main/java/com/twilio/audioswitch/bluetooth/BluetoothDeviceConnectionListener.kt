package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothDevice

internal interface BluetoothDeviceConnectionListener {
    fun onBluetoothConnected(bluetoothDevice: BluetoothDevice)
    fun onBluetoothDisconnected()
}