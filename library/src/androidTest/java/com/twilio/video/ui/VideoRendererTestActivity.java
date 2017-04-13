package com.twilio.video.ui;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;

import com.twilio.video.util.PermissionRequester;
import com.twilio.video.test.R;

import java.util.Arrays;
import java.util.List;

public class VideoRendererTestActivity extends Activity implements PermissionRequester {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_renderer_test_activity);
    }

    @Override
    public List<String> getNeededPermssions() {
        return Arrays.asList(Manifest.permission.CAMERA);
    }
}
