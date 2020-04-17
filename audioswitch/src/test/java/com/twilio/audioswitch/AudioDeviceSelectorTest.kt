package com.twilio.audioswitch

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.media.AudioManager
import com.nhaarman.mockitokotlin2.mock
import com.twilio.audioswitch.AudioDeviceSelector.State.STARTED
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetReceiver
import com.twilio.audioswitch.bluetooth.PreConnectedDeviceListener
import com.twilio.audioswitch.bluetooth.assertBluetoothControllerStart
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class AudioDeviceSelectorTest {

    private val context = mock<Context>()
    private val logger = mock<LogWrapper>()
    private val audioManager = mock<AudioManager>()
    private val bluetoothAdapter = mock<BluetoothAdapter>()
    private val preConnectedDeviceListener = PreConnectedDeviceListener(logger, bluetoothAdapter)
    private val bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger)
    private val audioDeviceSelector = AudioDeviceSelector(
            logger,
            audioManager,
            PhoneAudioDeviceManager(context, logger, audioManager),
            WiredHeadsetReceiver(context, logger),
            BluetoothController(
                    context,
                    audioManager,
                    bluetoothAdapter,
                    preConnectedDeviceListener,
                    bluetoothHeadsetReceiver)
    )

    @Test
    fun `start should transition to the started state if the current state is stopped`() {
        val audioDeviceChangeListener = mock<AudioDeviceChangeListener>()
        audioDeviceSelector.start(audioDeviceChangeListener)

        assertBluetoothControllerStart(
                context,
                preConnectedDeviceListener,
                bluetoothHeadsetReceiver,
                audioDeviceSelector.bluetoothDeviceConnectionListener,
                bluetoothAdapter)

        assertThat(audioDeviceSelector.state, equalTo(STARTED))
    }

    @Test
    fun `start should cache the default audio devices and the default selected audio device`() {
        val audioDeviceChangeListener = mock<AudioDeviceChangeListener>()
        audioDeviceSelector.start(audioDeviceChangeListener)

        val earpiece = AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece")
        val speakerphone = AudioDevice(AudioDevice.Type.SPEAKERPHONE, "Speakerphone")
        audioDeviceSelector.availableAudioDevices.let { audioDevices ->
            assertThat(audioDevices.size, equalTo(2))
            assertThat(audioDevices[0], equalTo(earpiece))
            assertThat(audioDevices[1], equalTo(speakerphone))
        }
        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(earpiece))
    }

    @Test
    fun `start should not start the BluetoothController if it is null`() {
        TODO("Not yet implemented")
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
    fun `start should do nothing if the current state is started`() {
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
}