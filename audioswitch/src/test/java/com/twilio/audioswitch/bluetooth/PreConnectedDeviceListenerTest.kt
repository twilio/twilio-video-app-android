package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.LogWrapper
import org.junit.Assert.fail
import org.junit.Test

class PreConnectedDeviceListenerTest {

    private val deviceListener = mock<BluetoothController.Listener>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private var preConnectedDeviceListener = PreConnectedDeviceListener(
            deviceListener,
            logger,
            bluetoothAdapter)

    @Test
    fun `onServiceConnected should notify the deviceListener`() {
        val expectedDevice = mock<BluetoothDevice>()
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

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

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

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
            preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)
        } catch (e: Exception) {
            fail(e.message)
        }
    }

    @Test
    fun `onServiceConnected should not not notify the deviceListener if there are no connected bluetooth devices`() {
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        preConnectedDeviceListener.onServiceDisconnected(0)

        verify(deviceListener).onBluetoothDisconnected()
    }
}