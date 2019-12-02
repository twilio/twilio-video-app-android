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
class TestWrapperAuthModule(
        private val firebaseWrapper: FirebaseWrapper,
        private val googleAuthWrapper: GoogleAuthWrapper,
        private val googleSignInWrapper: GoogleSignInWrapper,
        private val googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper,
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
    fun providesGoogleSignInOptionsBuilderWrapper(): GoogleSignInOptionsBuilderWrapper {
        return googleSignInOptionsBuilderWrapper
    }

    @Provides
    @ApplicationScope
    fun providesGoogleAuthProviderWrapper(): GoogleAuthProviderWrapper {
        return googleAuthProviderWrapper
    }

}
