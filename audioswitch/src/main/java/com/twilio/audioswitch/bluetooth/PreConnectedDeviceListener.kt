package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import com.twilio.audioswitch.LogWrapper

private const val TAG = "PreConnectedDeviceListener"

internal class PreConnectedDeviceListener(
    private val deviceListener: BluetoothDeviceConnectionListener,
    private val logger: LogWrapper,
    private val bluetoothAdapter: BluetoothAdapter
) : BluetoothProfile.ServiceListener {

    private var proxy: BluetoothProfile? = null

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        proxy = bluetoothProfile
        bluetoothProfile.connectedDevices.let { deviceList ->
            deviceList.forEach { device ->
                logger.d(TAG, "Bluetooth " + device.name + " connected")
                deviceListener.onBluetoothConnected(device)
            }
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        deviceListener.onBluetoothDisconnected()
    }

    fun stop() {
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, proxy)
    }
}
