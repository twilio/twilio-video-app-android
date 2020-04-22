package com.twilio.audioswitch

internal interface WiredDeviceConnectionListener {
    fun onDeviceConnected()
    fun onDeviceDisconnected()
}