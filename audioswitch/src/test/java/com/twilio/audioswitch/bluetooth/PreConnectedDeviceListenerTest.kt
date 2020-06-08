package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.LogWrapper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class PreConnectedDeviceListenerTest {

    private val deviceListener = mock<BluetoothDeviceConnectionListener>()
    private val logger = mock<LogWrapper>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private var preConnectedDeviceListener = PreConnectedDeviceListener(
            logger,
            bluetoothAdapter,
            deviceListener)

    @Test
    fun `onServiceConnected should notify the deviceListener`() {
        val expectedDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        deviceListener.onBluetoothConnected(isA())
    }

    @Test
    fun `onServiceConnected should notify the deviceListener with multiple devices`() {
        val expectedDevice = mock<BluetoothDevice> {
            whenever(mock.name).thenReturn("Test")
        }
        val bluetoothDevices = listOf(expectedDevice, expectedDevice)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        verify(deviceListener, times(2)).onBluetoothConnected(isA())
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if there are no connected bluetooth devices`() {
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(emptyList())
        }

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onServiceConnected should not notify the deviceListener if the deviceListener is null`() {
        preConnectedDeviceListener.deviceListener = null
        val expectedDevice = mock<BluetoothDevice>()
        val bluetoothDevices = listOf(expectedDevice)
        val bluetoothProfile = mock<BluetoothProfile> {
            whenever(mock.connectedDevices).thenReturn(bluetoothDevices)
        }

        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onServiceDisconnected should notify the deviceListener`() {
        preConnectedDeviceListener.onServiceDisconnected(0)

        verify(deviceListener).onBluetoothDisconnected()
    }

    @Test
    fun `onServiceDisconnected should not notify the deviceListener if deviceListener is null`() {
        preConnectedDeviceListener.deviceListener = null
        preConnectedDeviceListener.onServiceDisconnected(0)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `stop should close profile proxy`() {
        val bluetoothProfile = mock<BluetoothProfile>()
        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        preConnectedDeviceListener.stop()

        verify(bluetoothAdapter).closeProfileProxy(BluetoothProfile.HEADSET, bluetoothProfile)
    }

    @Test
    fun `stop should unasign the deviceListener`() {
        val bluetoothProfile = mock<BluetoothProfile>()
        preConnectedDeviceListener.onServiceConnected(0, bluetoothProfile)

        preConnectedDeviceListener.stop()

        assertThat(preConnectedDeviceListener.deviceListener, `is`(nullValue()))
    }
}
