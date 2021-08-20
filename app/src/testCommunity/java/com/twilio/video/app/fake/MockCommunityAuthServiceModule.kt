package com.twilio.video.app.fake

import android.content.SharedPreferences
import com.nhaarman.mockitokotlin2.mock
import com.twilio.video.app.android.SharedPreferencesWrapper
import com.twilio.video.app.data.CommunityAuthServiceModule
import com.twilio.video.app.data.api.AuthService
import com.twilio.video.app.data.api.AuthServiceRepository
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.security.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CommunityAuthServiceModule::class]
)
class MockCommunityAuthServiceModule {

    @Provides
    fun providesAuthService(): AuthService = mock()

    @Provides
    fun providesTokenService(
        authService: AuthService,
        securePreferences: SecurePreferences,
        sharedPreferences: SharedPreferences
    ): TokenService {
        return AuthServiceRepository(authService, securePreferences, SharedPreferencesWrapper(sharedPreferences))
    }
}
