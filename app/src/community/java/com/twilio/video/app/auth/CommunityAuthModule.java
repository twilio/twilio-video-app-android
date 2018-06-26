package com.twilio.video.app.auth;

import android.content.SharedPreferences;
import com.twilio.video.app.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class CommunityAuthModule {
  @Provides
  @ApplicationScope
  Authenticator providesAuthenticator(SharedPreferences preferences) {
    return new CommunityAuthenticator(preferences);
  }
}
