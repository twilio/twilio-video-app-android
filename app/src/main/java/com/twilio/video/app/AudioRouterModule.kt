package com.twilio.video.app

import android.app.Application
import com.twilio.audio_manager.AudioDeviceSelector
import dagger.Module
import dagger.Provides

@Module(includes = [ ApplicationModule::class ])
class AudioRouterModule {

    @Provides
    @ApplicationScope
    fun providesAudioDeviceSelector(application: Application): AudioDeviceSelector =
            AudioDeviceSelector(application.applicationContext)
}