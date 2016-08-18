package com.twilio.video.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RoomsTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hello Rooms!");
        setContentView(tv);
    }
}
