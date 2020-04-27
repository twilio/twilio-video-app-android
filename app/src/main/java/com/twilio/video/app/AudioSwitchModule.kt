package com.twilio.video.app

import android.app.Application
import com.twilio.audioswitch.selection.AudioDeviceSelector
import dagger.Module
import dagger.Provides

@Module(includes = [ ApplicationModule::class ])
class AudioSwitchModule {

    @Provides
    fun providesAudioDeviceSelector(application: Application): AudioDeviceSelector =
            AudioDeviceSelector(application)
}