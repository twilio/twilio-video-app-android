package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.IntentFilter
import android.media.AudioManager
import com.twilio.audioswitch.bluetooth.BluetoothDeviceReceiver
import com.twilio.audioswitch.bluetooth.PreConnectedDeviceListener

private const val TAG = "BluetoothController"

internal class BluetoothController(
    private val context: Context,
    private val audioManager: AudioManager,
    private val bluetoothAdapter: BluetoothAdapter,
    private val preConnectedDeviceListener: PreConnectedDeviceListener,
    private val bluetoothDeviceReceiver: BluetoothDeviceReceiver
) {

    companion object {

        fun newInstance(
            context: Context,
            logger: LogWrapper,
            deviceListener: Listener
        ): BluetoothController? =
            BluetoothAdapter.getDefaultAdapter()?.let { bluetoothAdapter ->
                BluetoothController(context,
                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager,
                        bluetoothAdapter,
                        PreConnectedDeviceListener(deviceListener, logger, bluetoothAdapter),
                        BluetoothDeviceReceiver(deviceListener, logger)
                )
            } ?: run {
                logger.d(TAG, "Bluetooth is not supported on this device")
                null
            }
    }

    fun start() {
        // Use the profile proxy to detect a bluetooth device that is already connected
        bluetoothAdapter.getProfileProxy(
                context,
                preConnectedDeviceListener,
                BluetoothProfile.HEADSET)

        // Register for bluetooth device connection and audio state changes
        context.run {
            registerReceiver(
                bluetoothDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
            registerReceiver(
                bluetoothDeviceReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            registerReceiver(
                bluetoothDeviceReceiver,
                IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))
        }
    }

    fun stop() {
        preConnectedDeviceListener.stop()
        context.unregisterReceiver(bluetoothDeviceReceiver)
    }

    fun activate() {
        audioManager.startBluetoothSco()
    }

    fun deactivate() {
        audioManager.stopBluetoothSco()
    }

    internal interface Listener {
        fun onBluetoothConnected(bluetoothDevice: BluetoothDevice)
        fun onBluetoothDisconnected()
    }
}