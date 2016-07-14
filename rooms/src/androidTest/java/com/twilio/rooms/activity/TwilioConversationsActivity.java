package com.twilio.rooms.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TwilioConversationsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hello Conversations!");
        setContentView(tv);
    }
}
