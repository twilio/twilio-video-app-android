package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.android.BluetoothDeviceWrapper

internal interface BluetoothDeviceConnectionListener {
    fun onBluetoothConnected(bluetoothDeviceWrapper: BluetoothDeviceWrapper)
    fun onBluetoothDisconnected()
    fun onBluetoothConnectionError(error: ConnectionError)

    sealed class ConnectionError {
        object SCO_CONNECTION_ERROR : ConnectionError()
        object SCO_DISCONNECTION_ERROR : ConnectionError()
    }
}
