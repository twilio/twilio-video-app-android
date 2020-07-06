package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.selection.AudioDevice
import kotlin.reflect.full.declaredFunctions

private const val TAG = "PreConnectedDeviceListener"

internal class PreConnectedDeviceListener(
    private val logger: LogWrapper,
    private val bluetoothAdapter: BluetoothAdapter,
    private val deviceCache: BluetoothDeviceCacheManager,
    var deviceListener: BluetoothDeviceConnectionListener? = null
) : BluetoothProfile.ServiceListener {

    private var headsetProxy: BluetoothHeadset? = null

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        headsetProxy = bluetoothProfile as BluetoothHeadset
        bluetoothProfile.connectedDevices.let { deviceList ->
            deviceList.forEach { device ->
                logger.d(TAG, "Bluetooth " + device.name + " connected")

                val bluetoothHeadset = AudioDevice.BluetoothHeadset(device.name,
                        BluetoothDeviceWrapperImpl(device))
                deviceCache.addDevice(bluetoothHeadset)
                deviceListener?.onBluetoothConnected()
            }
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
    }

    fun selectDevice(deviceWrapper: BluetoothDeviceWrapperImpl) {
        headsetProxy?.let { proxy ->
            val result = proxy::class.declaredFunctions.find { it.name == "setActiveDevice" }
                    ?.call(proxy, deviceWrapper.device) as Boolean
            if (result) logger.d(TAG, "Set the following bluetooth device to active: " +
                    deviceWrapper.name)
        }
    }

    fun stop() {
        deviceListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
    }
}
