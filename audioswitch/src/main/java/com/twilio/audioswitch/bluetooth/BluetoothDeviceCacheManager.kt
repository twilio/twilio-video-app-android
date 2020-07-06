package com.twilio.audioswitch.bluetooth

import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset

private const val TAG = "BluetoothDeviceCacheManager"

internal class BluetoothDeviceCacheManager(private val logger: LogWrapper) {

    private val mutableCachedDevices = mutableSetOf<BluetoothHeadset>()
    val cachedDevices: Set<BluetoothHeadset> get() = mutableCachedDevices

    fun addDevice(bluetoothHeadset: BluetoothHeadset) {
        val result = mutableCachedDevices.add(bluetoothHeadset)
        if(result) logger.d(TAG, "Add new bluetooth device to cache: ${bluetoothHeadset.name}")
    }

    fun removeDevice(bluetoothHeadset: BluetoothHeadset) {
        val result = mutableCachedDevices.remove(bluetoothHeadset)
        if(result) logger.d(TAG, "Remove bluetooth device from cache: ${bluetoothHeadset.name}")
    }
}