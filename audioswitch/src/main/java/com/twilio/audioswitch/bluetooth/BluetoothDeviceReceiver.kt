package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.twilio.audioswitch.LogWrapper

private const val TAG = "BluetoothDeviceReceiver"

internal class BluetoothDeviceReceiver(
    private val deviceListener: BluetoothDeviceConnectionListener,
    private val logger: LogWrapper
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val connectedBluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    if (isHeadsetDevice(connectedBluetoothDevice)) {
                        logger.d(
                                TAG,
                                "Bluetooth " +
                                        connectedBluetoothDevice.name +
                                        " connected")
                        deviceListener.onBluetoothConnected(connectedBluetoothDevice)
                    }
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val disconnectedBluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    logger.d(TAG, "Bluetooth disconnected")
                    deviceListener.onBluetoothDisconnected()
                }
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val state = intent.getIntExtra(
                            AudioManager.EXTRA_SCO_AUDIO_STATE,
                            AudioManager.SCO_AUDIO_STATE_ERROR)
                    if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                        logger.d(TAG, "Bluetooth Sco Audio connected")
                    } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                        logger.d(TAG, "Bluetooth Sco Audio disconnected")
                    }
                }
            }
        }
    }

    private fun isHeadsetDevice(bluetoothDevice: BluetoothDevice): Boolean {
        val bluetoothClass = bluetoothDevice.bluetoothClass
        if (bluetoothClass != null) {
            val deviceClass = bluetoothClass.deviceClass
            return deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO
        }
        return false
    }
}
