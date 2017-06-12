package com.twilio.video.base;

import android.os.Build;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.Camera2Capturer;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.ui.CameraCapturerTestActivity;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static org.junit.Assume.assumeTrue;

public abstract class BaseCamera2CapturerTest {
    protected static final int CAMERA2_CAPTURER_DELAY_MS = 3500;

    @Rule
    public ActivityTestRule<CameraCapturerTestActivity> activityRule =
            new ActivityTestRule<>(CameraCapturerTestActivity.class);
    protected CameraCapturerTestActivity cameraCapturerActivity;
    protected Camera2Capturer camera2Capturer;
    protected LocalVideoTrack localVideoTrack;
    protected FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        cameraCapturerActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(cameraCapturerActivity);
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
    }
}
