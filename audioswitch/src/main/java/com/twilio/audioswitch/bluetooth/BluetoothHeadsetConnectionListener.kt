package com.twilio.audioswitch.bluetooth

internal interface BluetoothHeadsetConnectionListener {
    fun onBluetoothHeadsetStateChanged(headsetName: String? = null)
    fun onBluetoothHeadsetActivationError()
}
