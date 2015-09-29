package com.twilio.rtc.conversations.sdktests;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.twilio.signal.TwilioRTC;

public class TwilioActivity extends AppCompatActivity implements TwilioRTC.InitListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

    }

    @Override
    public void onInitialized() {

    }

    @Override
    public void onError(Exception e) {

    }
}
