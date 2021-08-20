package com.twilio.video.app.security

import android.app.Application
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SecurityModule {

    @Provides
    @Singleton
    fun providesSecurePreferences(app: Application, preferences: SharedPreferences): SecurePreferences {
        return SecurePreferencesImpl(app.applicationContext, preferences)
    }
}
