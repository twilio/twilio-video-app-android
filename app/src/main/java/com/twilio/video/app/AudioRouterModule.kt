package com.twilio.video.app

import android.app.Application
import com.twilio.audio_router.AudioDeviceSelector
import dagger.Module
import dagger.Provides

@Module(includes = [ ApplicationModule::class ])
class AudioRouterModule {

    @Provides
    fun providesAudioDeviceSelector(application: Application): AudioDeviceSelector =
            AudioDeviceSelector(application.applicationContext)
}