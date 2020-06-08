package com.twilio.audioswitch

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.selection.AudioDevice.BluetoothHeadset
import com.twilio.audioswitch.selection.AudioDevice.Earpiece
import com.twilio.audioswitch.selection.AudioDevice.Speakerphone
import com.twilio.audioswitch.selection.AudioDeviceSelector
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class UserDeviceSelectionTest {

    private val context = InstrumentationRegistry.getInstrumentation().context

    @UiThreadTest
    @Test
    fun `it_should_select_the_earpiece_audio_device_when_the_user_selects_it`() {
        val audioDeviceSelector = AudioDeviceSelector(context)
        audioDeviceSelector.start { _, _ -> }
        val earpiece = audioDeviceSelector.availableAudioDevices
                .find { it is Earpiece }
        assertThat(earpiece, `is`(notNullValue()))

        audioDeviceSelector.selectDevice(earpiece!!)

        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(earpiece))
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_speakerphone_audio_device_when_the_user_selects_it`() {
        val audioDeviceSelector = AudioDeviceSelector(context)
        audioDeviceSelector.start { _, _ -> }
        val speakerphone = audioDeviceSelector.availableAudioDevices
                .find { it is Speakerphone }
        assertThat(speakerphone, `is`(notNullValue()))

        audioDeviceSelector.selectDevice(speakerphone!!)

        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(speakerphone))
    }

    @UiThreadTest
    @Test
    fun `it_should_select_the_bluetooth_audio_device_when_the_user_selects_it`() {
        val (audioDeviceSelector, bluetoothHeadsetReceiver) = setupFakeAudioDeviceSelector(context)
        audioDeviceSelector.start { _, _ -> }
        simulateBluetoothConnection(context, bluetoothHeadsetReceiver)
        val bluetoothDevice = audioDeviceSelector.availableAudioDevices
                .find { it is BluetoothHeadset }
        assertThat(bluetoothDevice, `is`(notNullValue()))

        audioDeviceSelector.selectDevice(bluetoothDevice!!)

        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(bluetoothDevice))
    }
}
