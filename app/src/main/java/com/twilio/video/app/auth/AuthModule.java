package com.twilio.video.app.auth;


import com.twilio.video.app.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class AuthModule {
    @Provides
    @ApplicationScope
    Authenticator providesAuthenticator() {
        return new FirebaseAuthenticator();
    }
}
