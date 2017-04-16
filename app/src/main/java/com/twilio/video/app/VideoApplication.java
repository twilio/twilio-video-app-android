package com.twilio.video.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.twilio.video.LogLevel;
import com.twilio.video.Video;
import com.twilio.video.app.util.DebugTree;
import com.twilio.video.app.util.FirebaseTreeRanger;
import com.twilio.video.app.util.ReleaseTree;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasDispatchingActivityInjector;
import timber.log.Timber;

public class VideoApplication extends Application implements HasDispatchingActivityInjector {
    public static final String HOCKEY_APP_ID = "11347c1df4dc4a929a1f6637fcbe64dc";

    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject Timber.Tree tree;

    private ApplicationComponent applicationComponent;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create application component and inject application
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        applicationComponent.inject(this);

        // Setup logging
        Timber.plant(tree);
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }
}
