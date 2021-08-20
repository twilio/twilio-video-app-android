package com.twilio.video.app.fake

import com.twilio.video.app.security.SecurePreferences
import com.twilio.video.app.security.SecurePreferencesFake
import com.twilio.video.app.security.SecurityModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [SecurityModule::class]
)
class FakeSecurityModule {

    @Provides
    @Singleton
    fun providesSecurePreferences(): SecurePreferences = SecurePreferencesFake()
}
