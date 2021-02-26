package com.twilio.video.app.ui;

import com.twilio.video.app.core.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class CommunityScreenSelectorModule {

    @Provides
    @ApplicationScope
    ScreenSelector providesScreenSelector() {
        return new CommunityScreenSelector();
    }
}
