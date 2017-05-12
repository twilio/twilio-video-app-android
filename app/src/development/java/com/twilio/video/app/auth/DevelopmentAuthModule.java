package com.twilio.video.app.auth;

import android.content.SharedPreferences;

import com.twilio.video.app.ApplicationScope;

import dagger.Module;
import dagger.Provides;

@Module
public class DevelopmentAuthModule {
    @Provides
    @ApplicationScope
    Authenticator providesAuthenticator(SharedPreferences preferences) {
        return new DevelopmentAuthenticator(preferences);
    }
}
