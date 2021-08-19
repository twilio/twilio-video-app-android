package com.twilio.video.app

import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleSignInOptionsWrapper
import com.twilio.video.app.auth.GoogleSignInWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class TestWrapperAuthModule(
    private val firebaseWrapper: FirebaseWrapper,
    private val googleAuthWrapper: GoogleAuthWrapper,
    private val googleSignInWrapper: GoogleSignInWrapper,
    private val googleSignInOptionsWrapper: GoogleSignInOptionsWrapper,
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper
) {

    @Provides
    fun providesFirebaseWrapper(): FirebaseWrapper {
        return firebaseWrapper
    }

    @Provides
    fun providesGoogleAuthWrapper(): GoogleAuthWrapper {
        return googleAuthWrapper
    }

    @Provides
    fun providesGoogleSignInWrapper(): GoogleSignInWrapper {
        return googleSignInWrapper
    }

    @Provides
    fun providesGoogleSignInOptionsWrapper(): GoogleSignInOptionsWrapper {
        return googleSignInOptionsWrapper
    }

    @Provides
    fun providesGoogleAuthProviderWrapper(): GoogleAuthProviderWrapper {
        return googleAuthProviderWrapper
    }
}
