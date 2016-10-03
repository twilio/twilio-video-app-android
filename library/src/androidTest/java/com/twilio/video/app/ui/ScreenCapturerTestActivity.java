package com.twilio.video.app.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;

import com.twilio.video.test.R;

import static org.junit.Assert.fail;

@TargetApi(21)
public class ScreenCapturerTestActivity extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 100;

    private int screenCaptureResultCode;
    private Intent screenCaptureIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_capturer_test_activity);
        requestScreenCapturePermission();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                fail("Failed to acquire screen capture permission");
                return;
            }

            this.screenCaptureResultCode = resultCode;
            this.screenCaptureIntent = data;
        }
    }

    public int getScreenCaptureResultCode() {
        return screenCaptureResultCode;
    }

    public Intent getScreenCaptureIntent() {
        return screenCaptureIntent;
    }

    private void requestScreenCapturePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }
}
