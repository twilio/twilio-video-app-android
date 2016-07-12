package com.tw.conv.testapp;

import android.app.Application;

import com.twilio.conversations.TwilioConversationsClient;

import timber.log.Timber;

public class TestAppApplication extends Application {
    public static final String HOCKEY_APP_ID = "f6f18860be18f5addf08df70c3760c2c";

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // We initialize the sdk here in case the application was destroyed
        if(!TwilioConversationsClient.isInitialized()) {
            Timber.d("Initializing conversations from application context");
            TwilioConversationsClient.initialize(this);
            Timber.d("Successfully initialized conversations");
        }
    }
}
