package com.twilio.video;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.FrameLayout;

import com.twilio.video.ui.ScreenCapturerTestActivity;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.twilio.video.test.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
@TargetApi(21)
public class ScreenCapturerTest {
    private static final int SCREEN_CAPTURER_DELAY = 3;

    @Rule
    public ActivityTestRule<ScreenCapturerTestActivity> activityRule =
            new ActivityTestRule<>(ScreenCapturerTestActivity.class);
    private ScreenCapturerTestActivity screenCapturerActivity;
    private LocalMedia localMedia;
    private ScreenCapturer screenCapturer;
    private LocalVideoTrack localVideoTrack;
    private FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        screenCapturerActivity = activityRule.getActivity();
        PermissionUtils.allowScreenCapture(false);
        localMedia = LocalMedia.create(screenCapturerActivity);
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        if (localMedia != null) {
            localMedia.removeLocalVideoTrack(localVideoTrack);
            localMedia.release();
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
    public void shouldCaptureFramesWhenVideoTrackAdded() throws InterruptedException {
        final CountDownLatch firstFrameReported = new CountDownLatch(1);
        ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
            @Override
            public void onScreenCaptureError(String errorDescription) {
                fail("Screen capture error not expected");
            }

            @Override
            public void onFirstFrameAvailable() {
                firstFrameReported.countDown();
            }
        };
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                screenCapturerListener);
        localVideoTrack = localMedia.addVideoTrack(true, screenCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(SCREEN_CAPTURER_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Validate first frame was reported
        assertTrue(firstFrameReported.await(SCREEN_CAPTURER_DELAY, TimeUnit.SECONDS));
    }

    @Test
    public void shouldStopCapturingFramesWhenVideoTrackRemoved() throws InterruptedException {
        final CountDownLatch firstFrameReported = new CountDownLatch(1);
        ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
            @Override
            public void onScreenCaptureError(String errorDescription) {
                fail("Screen capture error not expected");
            }

            @Override
            public void onFirstFrameAvailable() {
                firstFrameReported.countDown();
            }
        };
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                screenCapturerListener);
        localVideoTrack = localMedia.addVideoTrack(true, screenCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(SCREEN_CAPTURER_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Validate first frame was reported
        assertTrue(firstFrameReported.await(SCREEN_CAPTURER_DELAY, TimeUnit.SECONDS));

        // Remove video track and wait
        frameCount = frameCountRenderer.getFrameCount();
        localMedia.removeLocalVideoTrack(localVideoTrack);
        Thread.sleep(TimeUnit.SECONDS.toMillis(SCREEN_CAPTURER_DELAY));

        boolean framesNotRenderering = frameCount >= (frameCountRenderer.getFrameCount() - 1);
        assertTrue(framesNotRenderering);
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
            public void onFirstFrameAvailable() {
                fail("Do not expect to be capturing frames");
            }
        };
        // Create screen capturer with bogus result code
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                Integer.MIN_VALUE,
                null,
                screenCapturerListener);
        localVideoTrack = localMedia.addVideoTrack(true, screenCapturer);

        // We should be notified of an error because MediaProjection could not be accessed
        assertTrue(screenCaptureError.await(SCREEN_CAPTURER_DELAY, TimeUnit.SECONDS));
    }

    @Test
    public void canBeRenderedToView() throws InterruptedException {
        final FrameLayout localVideo =
                (FrameLayout) screenCapturerActivity
                        .findViewById(R.id.screen_capture_video_container);
        final AtomicReference<VideoViewRenderer> videoViewRendererReference =
                new AtomicReference<>();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        screenCapturerActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoViewRendererReference.set(new VideoViewRenderer(screenCapturerActivity,
                        localVideo));
            }
        });
        instrumentation.waitForIdleSync();
        VideoViewRenderer videoViewRenderer = videoViewRendererReference.get();
        final CountDownLatch renderedFirstFrame = new CountDownLatch(1);
        VideoRenderer.Listener rendererListener = new VideoRenderer.Listener() {
            @Override
            public void onFirstFrame() {
                renderedFirstFrame.countDown();
            }

            @Override
            public void onFrameDimensionsChanged(int width, int height, int rotation) {

            }
        };
        videoViewRenderer.setListener(rendererListener);
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                null);
        localVideoTrack = localMedia.addVideoTrack(true, screenCapturer);
        localVideoTrack.addRenderer(videoViewRenderer);

        assertTrue(renderedFirstFrame.await(2, TimeUnit.SECONDS));
        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
        videoViewRenderer.release();
    }
}
