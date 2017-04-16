package com.twilio.video.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twilio.video.app.auth.Authenticator;
import com.twilio.video.app.base.BaseActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SplashActivity extends BaseActivity {
    @Inject Authenticator authenticator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newIntent = authenticator.loggedIn() ?
            (new Intent(this, RoomActivity.class)) :
            (new Intent(this, authenticator.getLoginActivity()));
        startActivity(newIntent);
        finish();
    }

}
