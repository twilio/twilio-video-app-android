package com.twilio.audioswitch

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.selection.AudioDevice
import com.twilio.audioswitch.selection.AudioDeviceSelector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class AutomaticDeviceSelectionTest {

    @Test
    fun `it_should_select_the_earpiece_audio_device_by_default`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val audioDeviceSelector = AudioDeviceSelector(context)

        audioDeviceSelector.start { _, _ -> }

        val earpiece = AudioDevice(AudioDevice.Type.EARPIECE, "Earpiece")
        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(earpiece))
    }
}