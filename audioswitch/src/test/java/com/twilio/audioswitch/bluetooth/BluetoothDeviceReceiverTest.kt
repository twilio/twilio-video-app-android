package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_ERROR
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.LogWrapper
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class BluetoothDeviceReceiverTest {

    private val deviceListener = mock<BluetoothDeviceConnectionListener>()
    private val logger = mock<LogWrapper>()
    private var bluetoothDeviceReceiver = BluetoothDeviceReceiver(deviceListener, logger)

    fun parameters(): Array<Array<out Any?>> {
        val handsFreeDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE)
        }
        val audioVideoHeadsetDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)
        }
        val audioVideoCarDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO)
        }
        val wrongDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR)
        }
        return arrayOf(
            arrayOf(handsFreeDevice, true),
            arrayOf(audioVideoHeadsetDevice, true),
            arrayOf(audioVideoCarDevice, true),
            arrayOf(wrongDevice, false),
            arrayOf(null, false)
        )
    }

    @Parameters(method = "parameters")
    @Test
    fun `onReceive should register a new device when an ACL connected event is received`(
        deviceClass: BluetoothClass?,
        isNewDeviceConnected: Boolean
    ) {
        val bluetoothDevice = mock<BluetoothDevice> {
            whenever(mock.bluetoothClass).thenReturn(deviceClass)
        }
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothDeviceReceiver.onReceive(mock(), intent)

        val invocationCount = if (isNewDeviceConnected) 1 else 0
        verify(deviceListener, times(invocationCount)).onBluetoothConnected(bluetoothDevice)
    }

    @Test
    fun `onReceive should not register a new device when an ACL connected event is received with a null bluetooth device`() {
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(null)
        }

        bluetoothDeviceReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    @Parameters(method = "parameters")
    @Test
    fun `onReceive should disconnect a device when an ACL disconnected event is received`(
        deviceClass: BluetoothClass?,
        isDeviceDisconnected: Boolean
    ) {
        val bluetoothDevice = mock<BluetoothDevice> {
            whenever(mock.bluetoothClass).thenReturn(deviceClass)
        }
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothDeviceReceiver.onReceive(mock(), intent)

        val invocationCount = if (isDeviceDisconnected) 1 else 0
        verify(deviceListener, times(invocationCount)).onBluetoothDisconnected()
    }

    @Test
    fun `onReceive should not disconnect a device when an ACL disconnected event is received with a null bluetooth device`() {
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(null)
        }

        bluetoothDeviceReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    fun scoParameters(): Array<Array<Int>> {
        return arrayOf(
            arrayOf(SCO_AUDIO_STATE_CONNECTED),
            arrayOf(SCO_AUDIO_STATE_DISCONNECTED),
            arrayOf(SCO_AUDIO_STATE_ERROR)
        )
    }

    @Parameters(method = "scoParameters")
    @Test
    fun `onReceive should receive no device listener callbacks when an SCO audio event is received`(
        scoEvent: Int
    ) {
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            whenever(mock.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, SCO_AUDIO_STATE_ERROR))
                    .thenReturn(scoEvent)
        }

        bluetoothDeviceReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onReceive should receive no device listener callbacks if the intent action is null`() {
        bluetoothDeviceReceiver.onReceive(mock(), mock())

        verifyZeroInteractions(deviceListener)
    }
}