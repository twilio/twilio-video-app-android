package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset


internal interface BluetoothDeviceConnectionListener {
    fun onBluetoothConnected()
    fun onBluetoothDisconnected()
    fun onBluetoothConnectionError(error: ConnectionError)

    sealed class ConnectionError {
        object SCO_CONNECTION_ERROR : ConnectionError()
        object SCO_DISCONNECTION_ERROR : ConnectionError()
    }
}
