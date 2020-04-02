package com.twilio.video.app

import android.app.Application
import com.twilio.audioswitch.AudioDeviceSelector
import dagger.Module
import dagger.Provides

@Module(includes = [ ApplicationModule::class ])
class AudioRouterModule {

    @Provides
    fun providesAudioDeviceSelector(application: Application): AudioDeviceSelector =
            AudioDeviceSelector(application.applicationContext)
}