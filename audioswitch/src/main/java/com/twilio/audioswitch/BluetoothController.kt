package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log

private const val TAG = "BluetoothController"

internal class BluetoothController(private val context: Context, private val listener: Listener) {
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothDevice: BluetoothDevice? = null

    internal interface Listener {
        fun onBluetoothConnected(bluetoothDevice: BluetoothDevice)
        fun onBluetoothDisconnected()
    }

    fun start() {
        if (bluetoothAdapter != null) {
            // Use the profile proxy to detect a bluetooth device that is already connected
            bluetoothAdapter.getProfileProxy(
                    context,
                    object : BluetoothProfile.ServiceListener {
                        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                            val bluetoothDeviceList = proxy.connectedDevices
                            for (device in bluetoothDeviceList) {
                                Log.d(TAG, "Bluetooth " + device.name + " connected")
                                bluetoothDevice = device
                                listener.onBluetoothConnected(device)
                            }
                        }

                        override fun onServiceDisconnected(profile: Int) {
                            Log.d(TAG, "Bluetooth disconnected")
                            bluetoothDevice = null
                            listener.onBluetoothDisconnected()
                        }
                    },
                    BluetoothProfile.HEADSET)

            // Register for bluetooth device connection and audio state changes
            context.registerReceiver(
                    broadcastReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
            context.registerReceiver(
                    broadcastReceiver, IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            context.registerReceiver(
                    broadcastReceiver,
                    IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED))
        }
    }

    fun stop() {
        if (bluetoothAdapter != null) {
            context.unregisterReceiver(broadcastReceiver)
        }
    }

    fun activate() {
        audioManager.startBluetoothSco()
    }

    fun deactivate() {
        audioManager.stopBluetoothSco()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null) {
                when (action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val connectedBluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (isHeadsetDevice(connectedBluetoothDevice)) {
                            Log.d(
                                    TAG,
                                    "Bluetooth " +
                                            connectedBluetoothDevice.name +
                                            " connected")
                            bluetoothDevice = connectedBluetoothDevice
                            listener.onBluetoothConnected(bluetoothDevice!!)
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val disconnectedBluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        if (disconnectedBluetoothDevice == bluetoothDevice) {
                            bluetoothDevice = null
                        }
                        Log.d(TAG, "Bluetooth disconnected")
                        listener.onBluetoothDisconnected()
                    }
                    AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                        val state = intent.getIntExtra(
                                AudioManager.EXTRA_SCO_AUDIO_STATE,
                                AudioManager.SCO_AUDIO_STATE_ERROR)
                        if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
                            Log.d(TAG, "Bluetooth Sco Audio connected")
                        } else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
                            Log.d(TAG, "Bluetooth Sco Audio disconnected")
                        }
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