package com.twilio.video.app

import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleSignInOptionsWrapper
import com.twilio.video.app.auth.GoogleSignInWrapper
import dagger.Module
import dagger.Provides

@Module
class TestWrapperAuthModule(
    private val firebaseWrapper: FirebaseWrapper,
    private val googleAuthWrapper: GoogleAuthWrapper,
    private val googleSignInWrapper: GoogleSignInWrapper,
    private val googleSignInOptionsWrapper: GoogleSignInOptionsWrapper,
    private val googleAuthProviderWrapper: GoogleAuthProviderWrapper
) {

    @Provides
    @ApplicationScope
    fun providesFirebaseWrapper(): FirebaseWrapper {
        return firebaseWrapper
    }

    @Provides
    @ApplicationScope
    fun providesGoogleAuthWrapper(): GoogleAuthWrapper {
        return googleAuthWrapper
    }

    @Provides
    @ApplicationScope
    fun providesGoogleSignInWrapper(): GoogleSignInWrapper {
        return googleSignInWrapper
    }

    @Provides
    @ApplicationScope
    fun providesGoogleSignInOptionsWrapper(): GoogleSignInOptionsWrapper {
        return googleSignInOptionsWrapper
    }

    @Provides
    @ApplicationScope
    fun providesGoogleAuthProviderWrapper(): GoogleAuthProviderWrapper {
        return googleAuthProviderWrapper
    }
}
