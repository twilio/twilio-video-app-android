package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
import android.bluetooth.BluetoothClass.Device.Major.UNCATEGORIZED
import android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED
import android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED
import android.media.AudioManager.EXTRA_SCO_AUDIO_STATE
import android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_ERROR
import com.twilio.audioswitch.android.BluetoothDeviceWrapper
import com.twilio.audioswitch.android.BluetoothIntentProcessor
import com.twilio.audioswitch.android.LogWrapper

private const val TAG = "BluetoothDeviceReceiver"

internal class BluetoothHeadsetReceiver(
    private val context: Context,
    private val logger: LogWrapper,
    private val bluetoothIntentProcessor: BluetoothIntentProcessor,
    var deviceListener: BluetoothDeviceConnectionListener? = null
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.action?.let { action ->
            when (action) {
                ACTION_ACL_CONNECTED -> {
                    intent.getHeadsetDevice()?.let { bluetoothDevice ->
                        logger.d(
                                TAG,
                                "Bluetooth ACL device " +
                                        bluetoothDevice.name +
                                        " connected")
                        deviceListener?.onBluetoothConnected(bluetoothDevice)
                    }
                }
                ACTION_ACL_DISCONNECTED -> {
                    intent.getHeadsetDevice()?.let { bluetoothDevice ->
                        logger.d(
                                TAG,
                                "Bluetooth ACL device " +
                                        bluetoothDevice.name +
                                        " disconnected")
                        deviceListener?.onBluetoothDisconnected()
                    }
                }
                ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    intent.getIntExtra(EXTRA_SCO_AUDIO_STATE, SCO_AUDIO_STATE_ERROR).let { state ->
                        when (state) {
                            SCO_AUDIO_STATE_CONNECTED -> {
                                logger.d(TAG, "Bluetooth SCO Audio connected")
                            }
                            SCO_AUDIO_STATE_DISCONNECTED -> {
                                logger.d(TAG, "Bluetooth SCO Audio disconnected")
                            }
                            SCO_AUDIO_STATE_ERROR -> {
                                logger.e(TAG, "Error retrieving Bluetooth SCO Audio state")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    fun stop() {
        deviceListener = null
        context.unregisterReceiver(this)
    }

    private fun Intent.getHeadsetDevice(): BluetoothDeviceWrapper? =
            bluetoothIntentProcessor.getBluetoothDevice(this)?.let { device ->
                if (isHeadsetDevice(device)) device else null
            }

    private fun isHeadsetDevice(deviceWrapper: BluetoothDeviceWrapper): Boolean =
            deviceWrapper.deviceClass?.let { deviceClass ->
                deviceClass == AUDIO_VIDEO_HANDSFREE ||
                deviceClass == AUDIO_VIDEO_WEARABLE_HEADSET ||
                deviceClass == AUDIO_VIDEO_CAR_AUDIO ||
                deviceClass == AUDIO_VIDEO_HEADPHONES ||
                deviceClass == UNCATEGORIZED
            } ?: false
}
