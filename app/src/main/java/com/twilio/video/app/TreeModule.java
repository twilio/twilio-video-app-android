package com.twilio.video.app;

import com.twilio.video.LogLevel;
import com.twilio.video.Video;
import com.twilio.video.app.util.DebugTree;
import com.twilio.video.app.util.FirebaseTreeRanger;
import com.twilio.video.app.util.ReleaseTree;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

@Module
public class TreeModule {
    @Provides
    @ApplicationScope
    Timber.Tree providesTree(FirebaseTreeRanger treeRanger) {
        if (BuildConfig.DEBUG) {
            Video.setLogLevel(LogLevel.DEBUG);
            return new DebugTree(treeRanger);
        } else {
            return new ReleaseTree(treeRanger);
        }
    }
}
