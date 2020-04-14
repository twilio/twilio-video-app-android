package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.twilio.audioswitch.LogWrapper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class BluetoothControllerTest {

    private val context = mock<Context>()
    private val audioManager = mock<AudioManager>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val preConnectedDeviceListener = PreConnectedDeviceListener(logger, bluetoothAdapter)
    private val bluetoothDeviceReceiver = BluetoothHeadsetReceiver(context, logger)
    private var bluetoothController = BluetoothController(
            context,
            audioManager,
            bluetoothAdapter,
            preConnectedDeviceListener,
            bluetoothDeviceReceiver)

    @Test
    fun `start should register bluetooth listeners`() {
        val deviceListener = mock<BluetoothDeviceConnectionListener>()
        bluetoothController.start(deviceListener)

        assertThat(preConnectedDeviceListener.deviceListener, equalTo(deviceListener))
        assertThat(bluetoothDeviceReceiver.deviceListener, equalTo(deviceListener))
        verify(bluetoothAdapter).getProfileProxy(
                context,
                preConnectedDeviceListener,
                BluetoothProfile.HEADSET)
        verify(context, times(3)).registerReceiver(
                eq(bluetoothDeviceReceiver), isA())
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

        verify(context).unregisterReceiver(bluetoothDeviceReceiver)
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