package com.twilio.video.app;

import com.twilio.video.app.auth.FirebaseWrapper;
import com.twilio.video.app.auth.GoogleAuthProviderWrapper;
import com.twilio.video.app.auth.GoogleAuthWrapper;
import com.twilio.video.app.auth.GoogleSignInOptionsBuilderWrapper;
import com.twilio.video.app.auth.GoogleSignInWrapper;

import org.mockito.Mockito;

import dagger.Module;
import dagger.Provides;

@Module
public class TestWrapperAuthModule {

    @Provides
    @ApplicationScope
    FirebaseWrapper providesFirebaseWrapper() {
        return Mockito.mock(FirebaseWrapper.class);
    }

    @Provides
    @ApplicationScope
    GoogleAuthWrapper providesGoogleAuthWrapper() {
        return Mockito.mock(GoogleAuthWrapper.class);
    }

    @Provides
    @ApplicationScope
    GoogleSignInWrapper providesGoogleSignInWrapper() {
        return Mockito.mock(GoogleSignInWrapper.class);
    }

    @Provides
    @ApplicationScope
    GoogleSignInOptionsBuilderWrapper providesGoogleSignInOptionsBuilderWrapper() {
        return Mockito.mock(GoogleSignInOptionsBuilderWrapper.class);
    }

    @Provides
    @ApplicationScope
    GoogleAuthProviderWrapper providesGoogleAuthProviderWrapper() {
        return Mockito.mock(GoogleAuthProviderWrapper.class);
    }

}
