package com.twilio.video.ui;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.twilio.video.test.R;
import com.twilio.video.util.PermissionRequester;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

public class CameraCapturerTestActivity extends Activity implements PermissionRequester {
    private static final int REQUEST_CODE_CAMERA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_capturer_test_activity);
        requestCameraPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE_CAMERA) {
            boolean cameraPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (!cameraPermissionGranted) {
                fail("Failed to grant camera permission");
            }
        }
    }

    @Override
    public List<String> getNeededPermssions() {
        return Arrays.asList(Manifest.permission.CAMERA);
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean cameraPermissionGranted =
                    checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED;

            if (!cameraPermissionGranted) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            }
        }
    }
}
