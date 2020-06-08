package com.twilio.audioswitch.android

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.EXTRA_DEVICE
import android.content.Intent

internal class BluetoothIntentProcessorImpl : BluetoothIntentProcessor {

    override fun getBluetoothDevice(intent: Intent): BluetoothDeviceWrapper? =
        intent.getParcelableExtra<BluetoothDevice>(EXTRA_DEVICE)
                ?.let { device -> BluetoothDeviceWrapperImpl(device) }
}
