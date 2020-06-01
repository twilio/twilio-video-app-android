package com.twilio.video.app.security

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.ApplicationModule
import com.twilio.video.app.ApplicationScope
import com.twilio.video.app.data.DataModule
import dagger.Module
import dagger.Provides

@Module(includes = [
    ApplicationModule::class,
    DataModule::class
])
class SecurityModule {

    @Provides
    @ApplicationScope
    fun providesSecurePreferences(app: Application, preferences: SharedPreferences): SecurePreferences {
        return SecurePreferencesImpl(app.applicationContext, preferences)
    }
}
