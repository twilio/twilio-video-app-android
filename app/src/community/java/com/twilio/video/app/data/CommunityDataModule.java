package com.twilio.video.app.data;

import com.twilio.video.app.ApplicationScope;
import com.twilio.video.app.data.api.TokenService;
import dagger.Module;
import dagger.Provides;

@Module(includes = DataModule.class)
public class CommunityDataModule {
  @Provides
  @ApplicationScope
  TokenService providesTokenService() {
    return new CommunityTokenService();
  }
}
