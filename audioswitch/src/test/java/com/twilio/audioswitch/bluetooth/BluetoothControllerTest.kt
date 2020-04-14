package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.twilio.audioswitch.LogWrapper
import org.junit.Test

class BluetoothControllerTest {

    private val context = mock<Context>()
    private val audioManager = mock<AudioManager>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val preConnectedDeviceListener = PreConnectedDeviceListener(logger, bluetoothAdapter)
    private val bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger)
    private var bluetoothController = BluetoothController(
            context,
            audioManager,
            bluetoothAdapter,
            preConnectedDeviceListener,
            bluetoothHeadsetReceiver)

    @Test
    fun `start should register bluetooth listeners`() {
        val deviceListener = mock<BluetoothDeviceConnectionListener>()
        bluetoothController.start(deviceListener)

        assertBluetoothControllerStart(
                context,
                preConnectedDeviceListener,
                bluetoothHeadsetReceiver,
                deviceListener,
                bluetoothAdapter)
    }

    @Test
    fun `stop should close profile proxy`() {
        val bluetoothProfile = mock<BluetoothProfile>()
        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        bluetoothController.stop()

        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
    }

    @Test
    fun `stop should unregister the BroadcastReceiver`() {
        bluetoothController.stop()

        verify(context).unregisterReceiver(bluetoothHeadsetReceiver)
    }

    @Test
    fun `activate should start bluetooth device audio routing`() {
        bluetoothController.activate()

        verify(audioManager).startBluetoothSco()
    }

    @Test
    fun `deactivate should stop bluetooth device audio routing`() {
        bluetoothController.deactivate()

        verify(audioManager).stopBluetoothSco()
    }
}