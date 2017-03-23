package com.twilio.video.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.twilio.video.app.base.BaseActivity;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Intent newIntent = user == null ?
            (new Intent(this, LoginActivity.class)) :
            (new Intent(this, RoomActivity.class));
        startActivity(newIntent);
        finish();
    }

}
