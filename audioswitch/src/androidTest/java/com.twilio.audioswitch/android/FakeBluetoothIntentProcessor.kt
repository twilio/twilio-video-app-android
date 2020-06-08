package com.twilio.audioswitch.android

import android.content.Intent

internal class FakeBluetoothIntentProcessor : BluetoothIntentProcessor {

    override fun getBluetoothDevice(intent: Intent): BluetoothDeviceWrapper? {
        return FakeBluetoothDevice()
    }
}
