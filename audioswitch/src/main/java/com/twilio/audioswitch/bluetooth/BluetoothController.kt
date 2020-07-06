package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import com.twilio.audioswitch.android.BluetoothDeviceWrapper
import com.twilio.audioswitch.android.BluetoothDeviceWrapperImpl

internal class BluetoothController internal constructor(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val preConnectedDeviceListener: PreConnectedDeviceListener,
    private val bluetoothHeadsetReceiver: BluetoothHeadsetReceiver
) {

    fun start(deviceListener: BluetoothDeviceConnectionListener) {
        preConnectedDeviceListener.deviceListener = deviceListener
        bluetoothHeadsetReceiver.setupDeviceListener(deviceListener)

        bluetoothAdapter.getProfileProxy(
                context,
                preConnectedDeviceListener,
                BluetoothProfile.HEADSET)

        context.run {
            registerReceiver(
                    bluetoothHeadsetReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
            registerReceiver(
                    bluetoothHeadsetReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            registerReceiver(
                    bluetoothHeadsetReceiver,
                    IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))
        }
    }

    fun stop() {
        preConnectedDeviceListener.stop()
        bluetoothHeadsetReceiver.stop()
    }

    fun select(device: BluetoothDeviceWrapperImpl) {
        preConnectedDeviceListener.selectDevice(device)
    }
    
    fun activate() {
        bluetoothHeadsetReceiver.enableBluetoothSco(true)
    }

    fun deactivate() {
        bluetoothHeadsetReceiver.enableBluetoothSco(false)
    }
}
