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

private const val TAG = "BluetoothController"

internal class BluetoothController {
    internal var deviceListener: Listener? = null

    private val context: Context
    private val audioManager: AudioManager
    private lateinit var logger: LogWrapper
    private var bluetoothAdapter: BluetoothAdapter? = null

    constructor(context: Context) {
        this.context = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        logger = LogWrapper()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    internal constructor(
        context: Context,
        audioManager: AudioManager,
        logger: LogWrapper,
        bluetoothAdapter: BluetoothAdapter? = null
    ) {
        this.context = context
        this.audioManager = audioManager
        this.logger = logger
        this.bluetoothAdapter = bluetoothAdapter
    }

    internal interface Listener {
        fun onBluetoothConnected(bluetoothDevice: BluetoothDevice)
        fun onBluetoothDisconnected()
    }

    fun start() {
        bluetoothAdapter?.let { bluetoothAdapter ->
            // Use the profile proxy to detect a bluetooth device that is already connected
            bluetoothAdapter.getProfileProxy(
                    context,
                    bluetoothProfileListener,
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
        // TODO call closeProfileProxy
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

    internal val bluetoothProfileListener = object : BluetoothProfile.ServiceListener {

        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            proxy.connectedDevices.let { deviceList ->
                deviceList.forEach { device ->
                    logger.d(TAG, "Bluetooth " + device.name + " connected")
                    deviceListener?.onBluetoothConnected(device)
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            logger.d(TAG, "Bluetooth disconnected")
            deviceListener?.onBluetoothDisconnected()
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
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
                            deviceListener?.onBluetoothConnected(connectedBluetoothDevice)
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val disconnectedBluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        logger.d(TAG, "Bluetooth disconnected")
                        deviceListener?.onBluetoothDisconnected()
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