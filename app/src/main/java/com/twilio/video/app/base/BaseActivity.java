package com.twilio.video.app.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.common.base.Strings;
import com.twilio.video.app.BuildConfig;
import com.twilio.video.app.VideoApplication;
import com.twilio.video.app.util.BuildConfigUtils;

import net.hockeyapp.android.UpdateManager;

import dagger.android.AndroidInjection;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        if (registerForHockeyAppUpdates()) {
            UpdateManager.register(this, BuildConfig.HOCKEY_APP_ID);
        }
    }

    @Override
    protected void onDestroy() {
        if (registerForHockeyAppUpdates()) {
            UpdateManager.unregister();
        }
        super.onDestroy();
    }

    private boolean registerForHockeyAppUpdates() {
        return BuildConfigUtils.isInternalRelease() &&
                !Strings.isNullOrEmpty(BuildConfig.HOCKEY_APP_ID);
    }
}
