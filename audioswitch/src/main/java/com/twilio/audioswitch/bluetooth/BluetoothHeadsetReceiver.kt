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
import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.selection.AudioDeviceManager

private const val TAG = "BluetoothDeviceReceiver"

internal class BluetoothHeadsetReceiver(
    private val context: Context,
    private val logger: LogWrapper,
    private val bluetoothIntentProcessor: BluetoothIntentProcessor,
    audioDeviceManager: AudioDeviceManager,
    private val deviceCache: BluetoothDeviceCacheManager,
    private val enableBluetoothScoJob: BluetoothScoJob = EnableBluetoothScoJob(logger, audioDeviceManager),
    private val disableBluetoothScoJob: BluetoothScoJob = DisableBluetoothScoJob(logger, audioDeviceManager),
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
                        deviceCache.addDevice(BluetoothHeadset(bluetoothDevice.name, bluetoothDevice))
                        deviceListener?.onBluetoothConnected()
                    }
                }
                ACTION_ACL_DISCONNECTED -> {
                    intent.getHeadsetDevice()?.let { bluetoothDevice ->
                        logger.d(
                                TAG,
                                "Bluetooth ACL device " +
                                        bluetoothDevice.name +
                                        " disconnected")
                        deviceCache.removeDevice(BluetoothHeadset(bluetoothDevice.name, bluetoothDevice))
                        deviceListener?.onBluetoothDisconnected()
                    }
                }
                ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    intent.getIntExtra(EXTRA_SCO_AUDIO_STATE, SCO_AUDIO_STATE_ERROR).let { state ->
                        when (state) {
                            SCO_AUDIO_STATE_CONNECTED -> {
                                logger.d(TAG, "Bluetooth SCO Audio connected")
                                enableBluetoothScoJob.cancelBluetoothScoJob()
                            }
                            SCO_AUDIO_STATE_DISCONNECTED -> {
                                logger.d(TAG, "Bluetooth SCO Audio disconnected")
                                disableBluetoothScoJob.cancelBluetoothScoJob()
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

    fun enableBluetoothSco(enable: Boolean) {
        if (enable) {
            enableBluetoothScoJob.executeBluetoothScoJob()
        } else {
            disableBluetoothScoJob.executeBluetoothScoJob()
        }
    }

    fun setupDeviceListener(deviceListener: BluetoothDeviceConnectionListener) {
        this.deviceListener = deviceListener
        enableBluetoothScoJob.deviceListener = deviceListener
        disableBluetoothScoJob.deviceListener = deviceListener
    }

    fun stop() {
        deviceListener = null
        enableBluetoothScoJob.deviceListener = null
        disableBluetoothScoJob.deviceListener = null
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
