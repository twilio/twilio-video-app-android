package com.twilio.video.app.ui;

import com.twilio.video.app.ApplicationScope;
import com.twilio.video.app.ScreenSelector;

import dagger.Module;
import dagger.Provides;

@Module
public class CommunityScreenSelectorModule {

    @Provides
    @ApplicationScope
    ScreenSelector providesScreenSelector()  {
        return new CommunityScreenSelector();
    }
}
