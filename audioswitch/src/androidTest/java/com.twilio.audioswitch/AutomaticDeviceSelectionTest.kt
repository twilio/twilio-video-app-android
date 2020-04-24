package com.twilio.audioswitch

import android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.android.BuildWrapper
import com.twilio.audioswitch.android.FakeBluetoothIntentProcessor
import com.twilio.audioswitch.android.LogWrapper
import com.twilio.audioswitch.bluetooth.BluetoothController
import com.twilio.audioswitch.bluetooth.BluetoothHeadsetReceiver
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceManager
import com.twilio.audioswitch.selection.AudioDeviceSelector
import com.twilio.audioswitch.selection.AudioFocusRequestWrapper
import com.twilio.audioswitch.wired.WiredHeadsetReceiver
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AutomaticDeviceSelectionTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private lateinit var bluetoothHeadsetReceiver: BluetoothHeadsetReceiver

    @Test
    fun `it_should_select_the_bluetooth_audio_device_by_default`() {
        val audioDeviceSelector = setupFakeAudioDeviceSelector()
        val intent = Intent(ACTION_ACL_CONNECTED)

        audioDeviceSelector.start { _, _ -> }
        bluetoothHeadsetReceiver.onReceive(context, intent)

        val earpiece = AudioDevice(AudioDevice.Type.BLUETOOTH, "Fake Bluetooth")
        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(earpiece))
    }

    @Test
    fun `it_should_select_the_earpiece_audio_device_by_default`() {
        val audioDeviceSelector = AudioDeviceSelector(context)

        audioDeviceSelector.start { _, _ -> }

        val earpiece = AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece")
        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(earpiece))
    }

    private fun setupFakeAudioDeviceSelector(): AudioDeviceSelector {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val logger = LogWrapper()
        val audioDeviceManager =
                AudioDeviceManager(context,
                        logger,
                        audioManager,
                        BuildWrapper(),
                        AudioFocusRequestWrapper())
        val wiredHeadsetReceiver = WiredHeadsetReceiver(context, logger)
        val bluetoothIntentProcessor = FakeBluetoothIntentProcessor()
        bluetoothHeadsetReceiver = BluetoothHeadsetReceiver(context, logger, bluetoothIntentProcessor)
        val bluetoothController = BluetoothController.newInstance(
                context,
                logger,
                audioDeviceManager,
                bluetoothIntentProcessor,
                bluetoothHeadsetReceiver)
        return AudioDeviceSelector(logger,
                audioDeviceManager,
                wiredHeadsetReceiver,
                bluetoothController)
    }
}