package com.twilio.video.app;

import com.twilio.video.LogLevel;
import com.twilio.video.Video;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class DevelopmentTreeModule {
    @Provides
    @ApplicationScope
    Timber.Tree providesTree() {
        Video.setLogLevel(LogLevel.DEBUG);

        return new Timber.DebugTree();
    }
}
