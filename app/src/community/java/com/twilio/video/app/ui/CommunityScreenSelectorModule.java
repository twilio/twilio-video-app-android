package com.twilio.video.app.ui;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module
@InstallIn(ActivityComponent.class)
public class CommunityScreenSelectorModule {

    @Provides
    ScreenSelector providesScreenSelector() {
        return new CommunityScreenSelector();
    }
}
