/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.ui;

import static org.junit.Assert.fail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import com.twilio.video.test.R;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@TargetApi(21)
public class ScreenCapturerTestActivity extends Activity {
    private static final int REQUEST_MEDIA_PROJECTION = 100;
    private static final int PERMISSION_DELAY_MS = 25000;

    private int screenCaptureResultCode;
    private Intent screenCaptureIntent;
    private CountDownLatch screenCapturePermissionGranted = new CountDownLatch(1);

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
            screenCapturePermissionGranted.countDown();
        }
    }

    public int getScreenCaptureResultCode() {
        return screenCaptureResultCode;
    }

    public Intent getScreenCaptureIntent() {
        return screenCaptureIntent;
    }

    public boolean waitForPermissionGranted() {
        try {
            return screenCapturePermissionGranted.await(PERMISSION_DELAY_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void requestScreenCapturePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaProjectionManager mediaProjectionManager =
                    (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }
}
