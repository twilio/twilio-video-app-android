package com.tw.conv.testapp;

import android.app.Application;
import android.support.design.widget.Snackbar;
import android.widget.Toast;

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
            TwilioConversationsClient.initialize(this,
                    new TwilioConversationsClient.InitListener() {
                        @Override
                        public void onInitialized() {
                            Timber.d("Successfully initialized conversations");
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(TestAppApplication.this,
                                    R.string.error_initializing_conversations,
                                    Toast.LENGTH_LONG)
                                    .show();
                            throw new RuntimeException("Error initializing conversations: " +
                                    e.getLocalizedMessage());
                        }
                    });
        }
    }
}
