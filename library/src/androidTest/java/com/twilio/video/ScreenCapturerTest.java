package com.twilio.video;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.ui.ScreenCapturerTestActivity;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.twilio.video.test.R;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
@TargetApi(21)
public class ScreenCapturerTest {
    private static final int SCREEN_CAPTURER_DELAY_MS = 3000;

    @Rule
    public ActivityTestRule<ScreenCapturerTestActivity> activityRule =
            new ActivityTestRule<>(ScreenCapturerTestActivity.class);
    private ScreenCapturerTestActivity screenCapturerActivity;
    private LocalVideoTrack screenVideoTrack;
    private ScreenCapturer screenCapturer;

    @Before
    public void setup() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        screenCapturerActivity = activityRule.getActivity();
        PermissionUtils.allowScreenCapture(false);
    }

    @After
    public void teardown() {
        if (screenVideoTrack != null) {
            screenVideoTrack.release();
        }
    }

    @Test(expected = NullPointerException.class)
    public void constructorShouldFailWithNullContext() {
        screenCapturer = new ScreenCapturer(null,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                null);
    }

    @Test
    @Ignore
    public void shouldCaptureFramesWhenVideoTrackAdded() throws InterruptedException {
        final CountDownLatch firstFrameReported = new CountDownLatch(1);
        ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
            @Override
            public void onScreenCaptureError(String errorDescription) {}

            @Override
            public void onFirstFrameAvailable() {
                firstFrameReported.countDown();
            }
        };
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                screenCapturerListener);
        screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity, true, screenCapturer);

        // Validate first frame was reported
        assertTrue(firstFrameReported.await(SCREEN_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldRaiseErrorOnListenerIfMediaProjectionCannotBeAccessed()
            throws InterruptedException {
        final CountDownLatch screenCaptureError = new CountDownLatch(1);
        ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
            @Override
            public void onScreenCaptureError(String errorDescription) {
                screenCaptureError.countDown();
            }

            @Override
            public void onFirstFrameAvailable() {}
        };
        // Create screen capturer with bogus result code
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                Integer.MIN_VALUE,
                null,
                screenCapturerListener);
        screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity, true, screenCapturer);

        // We should be notified of an error because MediaProjection could not be accessed
        assertTrue(screenCaptureError.await(SCREEN_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    @Ignore
    public void canBeRenderedToView() throws InterruptedException {
        final VideoView localVideo =
                (VideoView) screenCapturerActivity
                        .findViewById(R.id.screen_capture_video);
        final CountDownLatch renderedFirstFrame = new CountDownLatch(1);
        VideoRenderer.Listener rendererListener = new VideoRenderer.Listener() {
            @Override
            public void onFirstFrame() {
                renderedFirstFrame.countDown();
            }

            @Override
            public void onFrameDimensionsChanged(int width, int height, int rotation) {}
        };
        localVideo.setListener(rendererListener);
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                null);
        screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity, true, screenCapturer);
        screenVideoTrack.addRenderer(localVideo);

        assertTrue(renderedFirstFrame.await(SCREEN_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    @Ignore
    public void canBeReused() throws InterruptedException {
        int reuseCount = 2;
        final AtomicReference<CountDownLatch> firstFrameReceived = new AtomicReference<>();
        ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
            @Override
            public void onScreenCaptureError(String errorDescription) {}

            @Override
            public void onFirstFrameAvailable() {
                firstFrameReceived.get().countDown();
            }
        };

        // Reuse the same capturer while we iterate
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                screenCapturerListener);

        for (int i = 0 ; i < reuseCount ; i++) {
            firstFrameReceived.set(new CountDownLatch(1));
            screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity, true, screenCapturer);

            // Validate we got our first frame
            assertTrue(firstFrameReceived.get().await(SCREEN_CAPTURER_DELAY_MS,
                    TimeUnit.MILLISECONDS));

            // Remove video track and wait
            screenVideoTrack.release();
        }
    }
}
