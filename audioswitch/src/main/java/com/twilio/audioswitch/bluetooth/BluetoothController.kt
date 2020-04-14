package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import com.twilio.audioswitch.LogWrapper

private const val TAG = "BluetoothController"

internal class BluetoothController internal constructor(
    private val context: Context,
    private val audioManager: AudioManager,
    private val bluetoothAdapter: BluetoothAdapter,
    private val preConnectedDeviceListener: PreConnectedDeviceListener,
    private val bluetoothHeadsetReceiver: BluetoothHeadsetReceiver
) {

    companion object {

        fun newInstance(
            context: Context,
            logger: LogWrapper
        ): BluetoothController? =
            BluetoothAdapter.getDefaultAdapter()?.let { bluetoothAdapter ->
                BluetoothController(context,
                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
                        bluetoothAdapter,
                        PreConnectedDeviceListener(logger, bluetoothAdapter),
                        BluetoothHeadsetReceiver(context, logger)
                )
            } ?: run {
                logger.d(TAG, "Bluetooth is not supported on this device")
                null
            }
    }

    fun start(deviceListener: BluetoothDeviceConnectionListener) {
        preConnectedDeviceListener.deviceListener = deviceListener
        bluetoothHeadsetReceiver.deviceListener = deviceListener

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

    fun activate() {
        audioManager.startBluetoothSco()
    }

    fun deactivate() {
        audioManager.stopBluetoothSco()
    }
}