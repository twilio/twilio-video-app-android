package com.twilio.video;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.ui.CameraCapturerTestActivity;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CameraCapturerTest {
    @Rule public ActivityTestRule<CameraCapturerTestActivity> activityRule =
            new ActivityTestRule<>(CameraCapturerTestActivity.class);
    private CameraCapturerTestActivity cameraCapturerActivity;
    private LocalMedia localMedia;
    private CameraCapturer cameraCapturer;
    private LocalVideoTrack localVideoTrack;
    private FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() throws InterruptedException {
        cameraCapturerActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(InstrumentationRegistry.getInstrumentation(),
                cameraCapturerActivity);
        localMedia = LocalMedia.create(cameraCapturerActivity);
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        localMedia.removeLocalVideoTrack(localVideoTrack);
        localMedia.release();
    }

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullContext() {
        cameraCapturer = CameraCapturer.create(null,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null);
    }

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullSource() {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, null, null);
    }

    @Test
    public void shouldCaptureFramesWhenAddedToVideoTrack() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait a second
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Validate our frame count is atleast 5
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount >= 5);
    }

    @Test
    public void shouldStopCapturingFramesWhenRemovedFromVideoTrack() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait a second
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Validate our frame count is atleast 5
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount >= 5);

        // Remove the renderer and wait
        frameCount = frameCountRenderer.getFrameCount();
        localVideoTrack.removeRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Ensure our camera capturer is no longer capturing frames
        assertEquals(frameCount, frameCountRenderer.getFrameCount());
    }
}
