package com.twilio.video.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import com.google.common.base.Strings;
import com.twilio.video.app.data.Preferences;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String identity = sharedPreferences.getString(Preferences.IDENTITY, null);
        Intent intent = Strings.isNullOrEmpty(identity) ?
                (new Intent(this, LoginActivity.class)) :
                (new Intent(this, RoomActivity.class));
        startActivity(intent);
        finish();
    }
}
