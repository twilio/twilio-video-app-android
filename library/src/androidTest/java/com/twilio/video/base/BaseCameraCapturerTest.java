package com.twilio.video.base;

import android.support.test.rule.ActivityTestRule;

import com.twilio.video.CameraCapturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.LogLevel;
import com.twilio.video.Video;
import com.twilio.video.ui.CameraCapturerTestActivity;
import com.twilio.video.util.DeviceUtils;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

public abstract class BaseCameraCapturerTest {
    protected static final int CAMERA_CAPTURE_DELAY_MS = 3000;

    @Rule
    public ActivityTestRule<CameraCapturerTestActivity> activityRule =
            new ActivityTestRule<>(CameraCapturerTestActivity.class);
    protected CameraCapturerTestActivity cameraCapturerActivity;
    protected CameraCapturer cameraCapturer;
    protected LocalVideoTrack localVideoTrack;
    protected FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() {
        // TODO: Resolve camera capturer test failures with Samsung Galaxy S3 GSDK-1080 GSDK-1110
        assumeFalse(DeviceUtils.isSamsungGalaxyS3());
        cameraCapturerActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(cameraCapturerActivity);
        Video.setLogLevel(LogLevel.ALL);
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
    }
}
