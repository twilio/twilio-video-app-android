package com.tw.conv.testapp;

import android.app.Application;

import timber.log.Timber;

public class TestAppApplication extends Application {

    public static final String HOCKEY_APP_ID = "f6f18860be18f5addf08df70c3760c2c";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
