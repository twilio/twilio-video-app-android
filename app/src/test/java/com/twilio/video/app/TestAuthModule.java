package com.twilio.video.app;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.twilio.video.app.auth.AuthenticationProvider;
import com.twilio.video.app.auth.Authenticator;
import com.twilio.video.app.auth.EmailAuthenticator;
import com.twilio.video.app.auth.FirebaseAuthenticator;
import com.twilio.video.app.auth.FirebaseWrapper;
import com.twilio.video.app.auth.GoogleAuthProviderWrapper;
import com.twilio.video.app.auth.GoogleAuthWrapper;
import com.twilio.video.app.auth.GoogleAuthenticator;
import com.twilio.video.app.auth.GoogleSignInOptionsBuilderWrapper;
import com.twilio.video.app.auth.GoogleSignInWrapper;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;

@Module(includes = {
        ApplicationModule.class,
        TestWrapperAuthModule.class
}
)
public class TestAuthModule {

    @Provides
    @ApplicationScope
    Authenticator providesAuthenticator(
            FirebaseWrapper firebaseWrapper,
            GoogleAuthWrapper googleAuthWrapper,
            GoogleSignInWrapper googleSignInWrapper,
            GoogleSignInOptionsBuilderWrapper googleSignInOptionsBuilderWrapper,
            GoogleAuthProviderWrapper googleAuthProviderWrapper,
            Application application,
            SharedPreferences sharedPreferences) {
        List<AuthenticationProvider> authenticators = new ArrayList<>();
        authenticators.add(
                new GoogleAuthenticator(
                        firebaseWrapper,
                        application,
                        googleAuthWrapper,
                        googleSignInWrapper,
                        googleSignInOptionsBuilderWrapper,
                        googleAuthProviderWrapper,
                        sharedPreferences)
        );
        authenticators.add(new EmailAuthenticator(firebaseWrapper, sharedPreferences));
        return new FirebaseAuthenticator(firebaseWrapper, authenticators);
    }
}
