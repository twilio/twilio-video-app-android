package com.twilio.video.app

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.auth.*
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
            application: Application,
            sharedPreferences: SharedPreferences
    ): Authenticator {
        val authenticators = ArrayList<AuthenticationProvider>()
        authenticators.add(
                GoogleAuthenticator(
                        firebaseWrapper,
                        application,
                        googleAuthWrapper,
                        googleSignInWrapper,
                        googleSignInOptionsWrapper,
                        googleAuthProviderWrapper)
        )
        authenticators.add(EmailAuthenticator(firebaseWrapper, sharedPreferences))
        return FirebaseAuthenticator(firebaseWrapper, authenticators)
    }
}
