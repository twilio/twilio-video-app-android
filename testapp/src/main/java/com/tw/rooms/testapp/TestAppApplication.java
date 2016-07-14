package com.tw.rooms.testapp;

import android.app.Application;

import timber.log.Timber;

public class TestAppApplication extends Application {
    public static final String HOCKEY_APP_ID = "11347c1df4dc4a929a1f6637fcbe64dc";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

    }
}
