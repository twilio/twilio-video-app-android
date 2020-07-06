package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl
import com.twilio.audioswitch.android.LogWrapper
import kotlin.reflect.full.declaredFunctions

private const val TAG = "PreConnectedDeviceListener"

internal class PreConnectedDeviceListener(
    private val logger: LogWrapper,
    private val bluetoothAdapter: BluetoothAdapter,
    var deviceListener: BluetoothDeviceConnectionListener? = null
) : BluetoothProfile.ServiceListener {

    private var headsetProxy: BluetoothHeadset? = null

    override fun onServiceConnected(profile: Int, bluetoothProfile: BluetoothProfile) {
        headsetProxy = bluetoothProfile as BluetoothHeadset
        bluetoothProfile.connectedDevices.let { deviceList ->
            deviceList.forEach { device ->
                logger.d(TAG, "Bluetooth " + device.name + " connected")
                deviceListener?.onBluetoothConnected(BluetoothDeviceWrapperImpl(device))
            }
        }
    }

    override fun onServiceDisconnected(profile: Int) {
        logger.d(TAG, "Bluetooth disconnected")
        deviceListener?.onBluetoothDisconnected()
    }

    fun connectHeadset(device: BluetoothDevice) {
        headsetProxy?.let { proxy ->
            proxy::class.declaredFunctions.find { it.name == "connect"}?.call(proxy, device)
            proxy::class.declaredFunctions.find { it.name == "setActiveDevice"}?.call(proxy, device)
        }
    }

    fun disconnectHeadset(device: BluetoothDevice) {
        headsetProxy?.let { proxy ->
            proxy::class.declaredFunctions.find { it.name == "disconnect" }?.call(proxy, device)
        }
    }

    fun stop() {
        deviceListener = null
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, headsetProxy)
    }
}
