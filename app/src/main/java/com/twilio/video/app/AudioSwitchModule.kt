package com.twilio.video.app

import android.app.Application
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AudioSwitchModule {

    @Provides
    fun providesAudioDeviceSelector(application: Application): AudioSwitch =
            AudioSwitch(application,
                    loggingEnabled = true,
                    preferredDeviceList = listOf(AudioDevice.BluetoothHeadset::class.java,
                            AudioDevice.WiredHeadset::class.java,
                            AudioDevice.Speakerphone::class.java,
                            AudioDevice.Earpiece::class.java))
}
