package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.fail
import org.junit.Test

class BluetoothControllerTest {

    private val context = mock<Context>()
    private val audioManager = mock<AudioManager>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val bluetoothController = BluetoothController(context, audioManager, logger, bluetoothAdapter)

    @Test
    fun `start should register the correct bluetooth receivers`() {
        bluetoothController.start()

        verify(bluetoothAdapter).getProfileProxy(isA(), isA(), isA())
        verify(context, times(3)).registerReceiver(isA(), isA())
    }

    @Test
    fun `onServiceConnected should notify the deviceListener`() {
        val expectedDevice = mock<BluetoothDevice>()
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }
        val deviceListener = mock<BluetoothController.Listener>()
        bluetoothController.deviceListener = deviceListener

        bluetoothController.bluetoothProfileListener.onServiceConnected(0, bluetoothProfile)

        deviceListener.onBluetoothConnected(expectedDevice)
    }

    @Test
    fun `onServiceConnected should notify the deviceListener with multiple devices`() {
        val expectedDevice1 = mock<BluetoothDevice>()
        val expectedDevice2 = mock<BluetoothDevice>()
        val bluetoothDevices = listOf(expectedDevice1, expectedDevice2)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }
        val deviceListener = mock<BluetoothController.Listener>()
        bluetoothController.deviceListener = deviceListener

        bluetoothController.bluetoothProfileListener.onServiceConnected(0, bluetoothProfile)

        verify(deviceListener).onBluetoothConnected(expectedDevice1)
        verify(deviceListener).onBluetoothConnected(expectedDevice2)
    }

    @Test
    fun `onServiceConnected should not throw an exception if the deviceListener is null`() {
        val expectedDevice = mock<BluetoothDevice>()
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        try {
            bluetoothController.bluetoothProfileListener.onServiceConnected(0, bluetoothProfile)
        } catch (e: Exception) {
            fail(e.message)
        }
    }

    @Test
    fun `onServiceConnected should not not notify the deviceListener if there are no connected bluetooth devices`() {
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }
        val deviceListener = mock<BluetoothController.Listener>()
        bluetoothController.deviceListener = deviceListener

        bluetoothController.bluetoothProfileListener.onServiceConnected(0, bluetoothProfile)

        verify(deviceListener, times(0)).onBluetoothConnected(any())
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        val deviceListener = mock<BluetoothController.Listener>()
        bluetoothController.deviceListener = deviceListener

        bluetoothController.bluetoothProfileListener.onServiceDisconnected(0)

        verify(deviceListener).onBluetoothDisconnected()
    }

    @Test
    fun `onServiceDisconnected should not throw an exception if the deviceListener is null`() {
        try {
            bluetoothController.bluetoothProfileListener.onServiceDisconnected(0)
        } catch (e: Exception) {
            fail(e.message)
        }
    }

    @Test
    fun `activate should start bluetooth device audio routing`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `activate should not start bluetooth device audio routing if a device hasn't been selected`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `deactivate should stop bluetooth device audio routing`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `deactivate should not stop bluetooth device audio routing if a device hasn't been selected`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `stop should remove all bluetooth listeners`() {
        TODO("Not yet implemented")
    }
}