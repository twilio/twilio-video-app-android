package com.twilio.audioswitch

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.twilio.audioswitch.selection.AudioDevice.Type.SPEAKERPHONE
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

    @Test
    fun `it_should_select_the_speakerphone_audio_device_when_the_user_selects_it`() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val audioDeviceSelector = AudioDeviceSelector(context)
        audioDeviceSelector.start { _, _ -> }
        val speakerphone = audioDeviceSelector.availableAudioDevices
                .find { it.type == SPEAKERPHONE }
        assertThat(speakerphone, `is`(notNullValue()))

        audioDeviceSelector.selectDevice(speakerphone!!)

        assertThat(audioDeviceSelector.selectedAudioDevice, equalTo(speakerphone))
    }
}