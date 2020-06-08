package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.selection.AudioDevice.Earpiece
import com.twilio.audioswitch.selection.AudioDeviceSelector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AutomaticDeviceSelectionTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    @UiThreadTest
    @Test
    fun `it_should_select_the_bluetooth_audio_device_by_default`() {
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(context)

        audioDeviceSelector.start { _, _ -> }
        simulateBluetoothConnection(context, bluetoothHeadsetReceiver)

        val bluetoothHeadset = BluetoothHeadset("Fake Bluetooth")
        assertEquals(bluetoothHeadset, audioDeviceSelector.selectedAudioDevice)
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_earpiece_audio_device_by_default`() {
        val audioDeviceSelector = AudioDeviceSelector(context)
        audioDeviceSelector.start { _, _ -> }

        assertThat(audioDeviceSelector.selectedAudioDevice is Earpiece, equalTo(true))
    }
}
