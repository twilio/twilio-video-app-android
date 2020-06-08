package com.twilio.audioswitch.bluetooth

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.SCO_AUDIO_STATE_CONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_DISCONNECTED
import android.media.AudioManager.SCO_AUDIO_STATE_ERROR
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.android.BluetoothIntentProcessorImpl
import com.twilio.audioswitch.android.LogWrapper
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(JUnitParamsRunner::class)
class BluetoothHeadsetReceiverTest {

    private val context = mock<Context>()
    private val deviceListener = mock<BluetoothDeviceConnectionListener>()
    private val logger = mock<LogWrapper>()
    private var bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger, BluetoothIntentProcessorImpl(), deviceListener)
    private val bluetoothClass = mock<BluetoothClass> {
        whenever(mock.deviceClass).thenReturn(AUDIO_VIDEO_HANDSFREE)
    }
    private val bluetoothDevice = mock<BluetoothDevice> {
        whenever(mock.name).thenReturn("Test")
        whenever(mock.bluetoothClass).thenReturn(bluetoothClass)
    }

    fun parameters(): Array<Array<out Any?>> {
        val handsFreeDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(AUDIO_VIDEO_HANDSFREE)
        }
        val audioVideoHeadsetDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)
        }
        val audioVideoCarDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO)
        }
        val headphonesDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
        }
        val uncategorizedDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.Major.UNCATEGORIZED)
        }
        val wrongDevice = mock<BluetoothClass> {
            whenever(mock.deviceClass).thenReturn(BluetoothClass.Device.AUDIO_VIDEO_VIDEO_MONITOR)
        }
        return arrayOf(
            arrayOf(handsFreeDevice, true),
            arrayOf(audioVideoHeadsetDevice, true),
            arrayOf(audioVideoCarDevice, true),
            arrayOf(headphonesDevice, true),
            arrayOf(uncategorizedDevice, true),
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
        whenever(bluetoothDevice.bluetoothClass).thenReturn(deviceClass)
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

        val invocationCount = if (isNewDeviceConnected) 1 else 0
        verify(deviceListener, times(invocationCount)).onBluetoothConnected(isA())
    }

    @Parameters(method = "parameters")
    @Test
    fun `onReceive should disconnect a device when an ACL disconnected event is received`(
        deviceClass: BluetoothClass?,
        isDeviceDisconnected: Boolean
    ) {
        whenever(bluetoothDevice.bluetoothClass).thenReturn(deviceClass)
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

        val invocationCount = if (isDeviceDisconnected) 1 else 0
        verify(deviceListener, times(invocationCount)).onBluetoothDisconnected()
    }

    @Test
    fun `onReceive should not register a new device when an ACL connected event is received with a null bluetooth device`() {
        whenever(bluetoothDevice.bluetoothClass).thenReturn(null)
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onReceive should not register a new device when the deviceListener is null`() {
        bluetoothHeadsetReceiver.deviceListener = null
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_CONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onReceive should not disconnect a device when an ACL disconnected event is received with a null bluetooth device`() {
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(null)
        }

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onReceive should not disconnect a device when the deviceListener is null`() {
        bluetoothHeadsetReceiver.deviceListener = null
        val intent = mock<Intent> {
            whenever(mock.action).thenReturn(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            whenever(mock.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE))
                    .thenReturn(bluetoothDevice)
        }

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

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

        bluetoothHeadsetReceiver.onReceive(mock(), intent)

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `onReceive should receive no device listener callbacks if the intent action is null`() {
        bluetoothHeadsetReceiver.onReceive(mock(), mock())

        verifyZeroInteractions(deviceListener)
    }

    @Test
    fun `stop should unassign the deviceListener`() {
        bluetoothHeadsetReceiver.stop()

        assertThat(bluetoothHeadsetReceiver.deviceListener, `is`(nullValue()))
    }

    @Test
    fun `stop should unregister the broadcast receiver`() {
        bluetoothHeadsetReceiver.stop()

        verify(context).unregisterReceiver(bluetoothHeadsetReceiver)
    }
}
