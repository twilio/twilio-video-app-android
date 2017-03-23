package com.twilio.video.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.twilio.video.LogLevel;
import com.twilio.video.VideoClient;
import com.twilio.video.app.util.DebugTree;
import com.twilio.video.app.util.FirebaseTreeRanger;
import com.twilio.video.app.util.ReleaseTree;

import timber.log.Timber;

public class VideoApplication extends Application {
    public static final String HOCKEY_APP_ID = "11347c1df4dc4a929a1f6637fcbe64dc";

    private final FirebaseTreeRanger firebaseTreeRanger = new FirebaseTreeRanger();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree(firebaseTreeRanger));
            VideoClient.setLogLevel(LogLevel.DEBUG);
        } else {
            Timber.plant(new ReleaseTree(firebaseTreeRanger));
        }
    }
}
