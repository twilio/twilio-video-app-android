package com.twilio.video.app.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.twilio.video.BuildConfig;
import com.twilio.video.app.TestAppApplication;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            UpdateManager.register(this, TestAppApplication.HOCKEY_APP_ID);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashManager.register(this, TestAppApplication.HOCKEY_APP_ID);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!BuildConfig.DEBUG) {
            UpdateManager.unregister();
        }
    }

}
