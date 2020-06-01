package com.twilio.video.app

import android.app.Application
import com.twilio.video.app.auth.AuthenticationProvider
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.auth.EmailAuthProvider
import com.twilio.video.app.auth.FirebaseAuthenticator
import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProvider
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleSignInOptionsWrapper
import com.twilio.video.app.auth.GoogleSignInWrapper
import dagger.Module
import dagger.Provides

@Module(includes = [
    ApplicationModule::class,
    TestWrapperAuthModule::class
])
class TestAuthModule {

    @Provides
    @ApplicationScope
    internal fun providesAuthenticator(
        firebaseWrapper: FirebaseWrapper,
        googleAuthWrapper: GoogleAuthWrapper,
        googleSignInWrapper: GoogleSignInWrapper,
        googleSignInOptionsWrapper: GoogleSignInOptionsWrapper,
        googleAuthProviderWrapper: GoogleAuthProviderWrapper,
        application: Application
    ): Authenticator {
        val authenticators = ArrayList<AuthenticationProvider>()
        authenticators.add(
                GoogleAuthProvider(
                        firebaseWrapper,
                        application,
                        googleAuthWrapper,
                        googleSignInWrapper,
                        googleSignInOptionsWrapper,
                        googleAuthProviderWrapper)
        )
        authenticators.add(EmailAuthProvider(firebaseWrapper))
        return FirebaseAuthenticator(firebaseWrapper, authenticators)
    }
}
