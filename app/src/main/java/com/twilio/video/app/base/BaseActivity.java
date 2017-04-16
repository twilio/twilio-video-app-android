package com.twilio.video.app.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.VideoApplication;

import net.hockeyapp.android.UpdateManager;

import dagger.android.AndroidInjection;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String INTERNAL_FLAVOR = "internal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        if (internalRelease()) {
            UpdateManager.register(this, VideoApplication.HOCKEY_APP_ID);
        }
    }

    @Override
    protected void onDestroy() {
        if (internalRelease()) {
            UpdateManager.unregister();
        }
        super.onDestroy();
    }

    private boolean internalRelease() {
        return BuildConfig.FLAVOR.equals(INTERNAL_FLAVOR) && !BuildConfig.DEBUG;
    }
}
