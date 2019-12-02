package com.twilio.video.app

import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleSignInOptionsBuilderWrapper
import com.twilio.video.app.auth.GoogleSignInWrapper

import org.mockito.Mockito

import dagger.Module
import dagger.Provides

@Module
class TestWrapperAuthModule {

    @Provides
    @ApplicationScope
    internal fun providesFirebaseWrapper(): FirebaseWrapper {
        return Mockito.mock(FirebaseWrapper::class.java)
    }

    @Provides
    @ApplicationScope
    internal fun providesGoogleAuthWrapper(): GoogleAuthWrapper {
        return Mockito.mock(GoogleAuthWrapper::class.java)
    }

    @Provides
    @ApplicationScope
    internal fun providesGoogleSignInWrapper(): GoogleSignInWrapper {
        return Mockito.mock(GoogleSignInWrapper::class.java)
    }

    @Provides
    @ApplicationScope
    internal fun providesGoogleSignInOptionsBuilderWrapper(): GoogleSignInOptionsBuilderWrapper {
        return Mockito.mock(GoogleSignInOptionsBuilderWrapper::class.java)
    }

    @Provides
    @ApplicationScope
    internal fun providesGoogleAuthProviderWrapper(): GoogleAuthProviderWrapper {
        return Mockito.mock(GoogleAuthProviderWrapper::class.java)
    }

}
