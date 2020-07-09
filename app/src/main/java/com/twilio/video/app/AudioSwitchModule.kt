package com.twilio.video.app

import android.app.Application
import android.media.AudioManager.OnAudioFocusChangeListener
import com.twilio.audioswitch.AudioSwitch
import dagger.Module
import dagger.Provides
import timber.log.Timber

@Module(includes = [ ApplicationModule::class ])
class AudioSwitchModule {

    @Provides
    fun providesAudioSwitch(application: Application): AudioSwitch =
            AudioSwitch(application, true, OnAudioFocusChangeListener { i ->
                Timber.d("Audio focus changed to: $i")
            })
}
