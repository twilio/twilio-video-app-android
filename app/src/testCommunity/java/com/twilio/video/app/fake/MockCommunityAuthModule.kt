package com.twilio.video.app.fake

import android.content.SharedPreferences
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.auth.CommunityAuthModule
import com.twilio.video.app.auth.CommunityAuthenticator
import com.twilio.video.app.data.api.TokenService
import com.twilio.video.app.security.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.Dispatchers

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CommunityAuthModule::class]
)
class MockCommunityAuthModule {
    @Provides
    fun providesCommunityAuthenticator(
        preferences: SharedPreferences,
        securePreferences: SecurePreferences,
        tokenService: TokenService
    ): Authenticator {
        return CommunityAuthenticator(preferences, securePreferences, tokenService, Dispatchers.Main)
    }
}
