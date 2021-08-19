package com.twilio.video.app.ui;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class CommunityScreenSelectorModule {

    @Provides
    ScreenSelector providesScreenSelector() {
        return new CommunityScreenSelector();
    }
}
