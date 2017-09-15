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

package com.twilio.video.base;

import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LogLevel;
import com.twilio.video.Video;
import com.twilio.video.ui.CameraCapturerTestActivity;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public abstract class BaseCameraCapturerTest {
    protected static final int CAMERA_CAPTURE_DELAY_MS = 3000;

    @Rule
    public ActivityTestRule<CameraCapturerTestActivity> activityRule =
            new ActivityTestRule<>(CameraCapturerTestActivity.class);
    protected CameraCapturerTestActivity cameraCapturerActivity;
    protected CameraCapturer cameraCapturer;
    protected LocalVideoTrack localVideoTrack;
    protected FrameCountRenderer frameCountRenderer;
    protected CameraCapturer.CameraSource supportedCameraSource;

    @Before
    public void setup() {
        cameraCapturerActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(cameraCapturerActivity);
        Video.setLogLevel(LogLevel.ALL);
        frameCountRenderer = new FrameCountRenderer();
        supportedCameraSource = getSupportedCameraSource();
        assumeTrue(supportedCameraSource != null);
    }

    @After
    public void teardown() {
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
    }

    protected boolean bothCameraSourcesAvailable() {
        boolean bothCamerasSupported =
                CameraCapturer.isSourceAvailable(CameraCapturer.CameraSource.FRONT_CAMERA) &&
                CameraCapturer.isSourceAvailable(CameraCapturer.CameraSource.BACK_CAMERA);

        /*
         * Validate that if both cameras are not supported that there are actually less than
         * two cameras reported by framework.
         */
        if (!bothCamerasSupported) {
            assertTrue(Camera.getNumberOfCameras() < 2);
        }

        return bothCamerasSupported;
    }

    private @Nullable CameraCapturer.CameraSource getSupportedCameraSource() {
        if (CameraCapturer.isSourceAvailable(CameraCapturer.CameraSource.FRONT_CAMERA)) {
            return CameraCapturer.CameraSource.FRONT_CAMERA;
        } else if (CameraCapturer.isSourceAvailable(CameraCapturer.CameraSource.BACK_CAMERA)) {
            return CameraCapturer.CameraSource.BACK_CAMERA;
        } else {
            Log.w("BaseCameraCapturerTest", "No supported camera source");
            return null;
        }
    }
}
