package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.twilio.audioswitch.AudioDeviceSelector.State.STARTED
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetReceiver
import com.twilio.audioswitch.bluetooth.PreConnectedDeviceListener
import com.twilio.audioswitch.bluetooth.assertBluetoothControllerNotStarted
import com.twilio.audioswitch.bluetooth.assertBluetoothControllerStart
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.fail
import org.junit.Test

class AudioDeviceSelectorTest {

    private val earpiece = AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece")
    private val speakerphone = AudioDevice(AudioDevice.Type.SPEAKERPHONE, "Speakerphone")
    private val packageManager = mock<PackageManager> {
        whenever(mock.hasSystemFeature(any())).thenReturn(true)
    }
    private val context = mock<Context> {
        whenever(mock.packageManager).thenReturn(packageManager)
    }
    private val logger = mock<LogWrapper>()
    private val audioManager = mock<AudioManager>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val audioDeviceChangeListener = mock<AudioDeviceChangeListener>()
    private val preConnectedDeviceListener = PreConnectedDeviceListener(logger, bluetoothAdapter)
    private val bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger)
    private val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
    private var audioDeviceSelector = AudioDeviceSelector(
            logger,
            audioManager,
            PhoneAudioDeviceManager(context, logger, audioManager),
            wiredHeadsetReceiver,
            BluetoothController(
                    context,
                    audioManager,
                    bluetoothAdapter,
                    preConnectedDeviceListener,
                    bluetoothHeadsetReceiver)
    )

    @Test
    fun `start should start the bluetooth and wired headset listeners`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        assertBluetoothControllerStart(
                context,
                preConnectedDeviceListener,
                bluetoothHeadsetReceiver,
                audioDeviceSelector.bluetoothDeviceConnectionListener,
                bluetoothAdapter)

        assertThat(wiredHeadsetReceiver.deviceListener, equalTo(audioDeviceSelector.wiredDeviceConnectionListener))
        verify(context).registerReceiver(eq(wiredHeadsetReceiver), isA())
    }

    @Test
    fun `start should transition to the started state if the current state is stopped`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        assertThat(audioDeviceSelector.state, equalTo(STARTED))
    }

    @Test
    fun `start should cache the default audio devices and the default selected audio device`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        audioDeviceSelector.availableAudioDevices.let { audioDevices ->
            assertThat(audioDevices.size, equalTo(2))
            assertThat(audioDevices[0], equalTo(earpiece))
            assertThat(audioDevices[1], equalTo(speakerphone))
        }
        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(earpiece))
    }

    @Test
    fun `start should invoke the audio device change listener with the default audio devices`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        verify(audioDeviceChangeListener).invoke(
                listOf(earpiece, speakerphone),
                earpiece)
    }

    @Test
    fun `start should not start the BluetoothController if it is null`() {
        audioDeviceSelector = AudioDeviceSelector(
                logger,
                audioManager,
                PhoneAudioDeviceManager(context, logger, audioManager),
                wiredHeadsetReceiver,
                bluetoothController = null
        )

        audioDeviceSelector.start(audioDeviceChangeListener)

        assertBluetoothControllerNotStarted(
                context,
                preConnectedDeviceListener,
                bluetoothHeadsetReceiver,
                bluetoothAdapter)
    }

    @Test
    fun `start should do nothing if the current state is started`() {
        audioDeviceSelector.start(audioDeviceChangeListener)

        try {
            audioDeviceSelector.start(audioDeviceChangeListener)
        } catch (e: Exception) {
            fail("Exception should not have been thrown")
        }
    }

    @Test
    fun `stop should do nothing if the current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `deactivate should do nothing if the current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `activate should throw an IllegalStateException if the current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `selectDevice should throw an IllegalStateException if the current state is stopped`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `stop should transition to the stopped state if the current state is started`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `deactivate should throw an IllegalStateException if the current state is started`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `activate should transition to the activated state if the current state is started`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `activate should do nothing if the current state is is activated`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `selectDevice should do nothing if the current state is activated`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `start should do nothing if the current state is activated`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `stop should transition to the stopped state if the current state is activated`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `stop should unassign the audio device change listener`() {
        TODO("Not yet implemented")
    }

    @Test
    fun `TODO test all permutations of possible audio devices and their priorities`() {
        TODO("Not yet implemented")
    }
}