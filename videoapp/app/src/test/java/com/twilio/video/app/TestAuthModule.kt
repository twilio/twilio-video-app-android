package com.twilio.video.app

import android.app.Application
import android.content.SharedPreferences
import com.twilio.video.app.auth.AuthenticationProvider
import com.twilio.video.app.auth.Authenticator
import com.twilio.video.app.auth.EmailAuthenticator
import com.twilio.video.app.auth.FirebaseAuthenticator
import com.twilio.video.app.auth.FirebaseWrapper
import com.twilio.video.app.auth.GoogleAuthProviderWrapper
import com.twilio.video.app.auth.GoogleAuthWrapper
import com.twilio.video.app.auth.GoogleAuthenticator
import com.twilio.video.app.auth.GoogleSignInOptionsBuilderWrapper
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
        googleSignInOptionsBuilderWrapper: GoogleSignInOptionsBuilderWrapper,
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
                        googleSignInOptionsBuilderWrapper,
                        googleAuthProviderWrapper,
                        sharedPreferences)
        )
        authenticators.add(EmailAuthenticator(firebaseWrapper, sharedPreferences))
        return FirebaseAuthenticator(firebaseWrapper, authenticators)
    }
}
