package com.twilio.video.app.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.twilio.video.app.VideoApplication;
import com.twilio.video.app.util.BuildConfigUtils;

import net.hockeyapp.android.UpdateManager;

import dagger.android.AndroidInjection;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String INTERNAL_FLAVOR = "internal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        if (BuildConfigUtils.isInternalRelease()) {
            UpdateManager.register(this, VideoApplication.HOCKEY_APP_ID);
        }
    }

    @Override
    protected void onDestroy() {
        if (BuildConfigUtils.isInternalRelease()) {
            UpdateManager.unregister();
        }
        super.onDestroy();
    }
}
