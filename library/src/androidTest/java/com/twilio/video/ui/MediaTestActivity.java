package com.twilio.video.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MediaTestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hello Rooms!");
        setContentView(tv);
    }
}
