package com.twilio.video.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.twilio.video.util.PermissionRequester;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

public class MediaTestActivity extends Activity implements PermissionRequester {
    private static final int REQUEST_CODE_RECORD_AUDIO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Hello Rooms!");
        setContentView(tv);
        requestRecordAudioPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_RECORD_AUDIO) {
            boolean recordAudioPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (!recordAudioPermissionGranted) {
                fail("Failed to grant record audio permission");
            }
        }
    }

    @Override
    public List<String> getNeededPermssions() {
        return Arrays.asList(Manifest.permission.RECORD_AUDIO);
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean recordAudioPermissionGranted =
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED;

            if (!recordAudioPermissionGranted) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_RECORD_AUDIO);
            }
        }
    }
}
