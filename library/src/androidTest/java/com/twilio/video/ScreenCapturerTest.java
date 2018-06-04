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

package com.twilio.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.ui.ScreenCapturerTestActivity;

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
import com.twilio.video.util.DeviceUtils;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
@TargetApi(21)
public class ScreenCapturerTest extends BaseVideoTest {
    private static final int SCREEN_CAPTURER_DELAY_MS = 4000;
    private static final int PERMISSIONS_DIALOG_DELAY_MS = 2000;
    private static final String START_CAPTURE_BUTTON_ID = "android:id/button1";

    @Rule
    public ActivityTestRule<ScreenCapturerTestActivity> activityRule =
            new ActivityTestRule<>(ScreenCapturerTestActivity.class);
    private ScreenCapturerTestActivity screenCapturerActivity;
    private LocalVideoTrack screenVideoTrack;
    private ScreenCapturer screenCapturer;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        screenCapturerActivity = activityRule.getActivity();
        allowScreenCapture();
    }

    @After
    public void teardown() {
        if (screenVideoTrack != null) {
            screenVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void getSupportedFormats_shouldReturnDimensionsBasedOnScreenSize() {
        ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
            @Override
            public void onScreenCaptureError(String errorDescription) {}

            @Override
            public void onFirstFrameAvailable() {
            }
        };
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager =
                (WindowManager) screenCapturerActivity.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        VideoDimensions expectedDimensions = new VideoDimensions(displayMetrics.widthPixels,
                displayMetrics.heightPixels);
        screenCapturer = new ScreenCapturer(screenCapturerActivity,
                screenCapturerActivity.getScreenCaptureResultCode(),
                screenCapturerActivity.getScreenCaptureIntent(),
                screenCapturerListener);

        assertEquals(expectedDimensions, screenCapturer.getSupportedFormats().get(0).dimensions);
    }

    @Test
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
                new Intent(),
                screenCapturerListener);
        screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity, true, screenCapturer);

        // We should be notified of an error because MediaProjection could not be accessed
        assertTrue(screenCaptureError.await(SCREEN_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    @Ignore
    public void shouldInvokeScreenCapturerListenerCallbacksOnCreationThread()
            throws InterruptedException {
        final CountDownLatch screenCaptureError = new CountDownLatch(1);
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        /*
         * Run on UI thread to avoid thread hopping between the test runner thread and the UI
         * thread.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final long callingThreadId = Thread.currentThread().getId();
                ScreenCapturer.Listener screenCapturerListener = new ScreenCapturer.Listener() {
                    @Override
                    public void onScreenCaptureError(String errorDescription) {
                        assertEquals(callingThreadId, Thread.currentThread().getId());
                        screenCaptureError.countDown();
                    }

                    @Override
                    public void onFirstFrameAvailable() {
                        assertEquals(callingThreadId, Thread.currentThread().getId());
                        firstFrameAvailable.countDown();
                    }
                };

                // Validate error callback is invoked on correct thread
                screenCapturer = new ScreenCapturer(screenCapturerActivity,
                        Integer.MIN_VALUE,
                        new Intent(),
                        screenCapturerListener);
                screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity,
                        true, screenCapturer);

                // Validate first frame event is invoked on correct thread
                screenCapturer = new ScreenCapturer(screenCapturerActivity,
                        screenCapturerActivity.getScreenCaptureResultCode(),
                        screenCapturerActivity.getScreenCaptureIntent(),
                        screenCapturerListener);
                screenVideoTrack = LocalVideoTrack.create(screenCapturerActivity,
                        true, screenCapturer);
            }
        });

        assertTrue(screenCaptureError.await(SCREEN_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
        assertTrue(firstFrameAvailable.await(SCREEN_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
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
    public void canBeReused() throws InterruptedException {
        // TODO: Fix test on Nexus 9 API 21
        assumeFalse(DeviceUtils.isNexus9() && Build.VERSION.SDK_INT ==
                Build.VERSION_CODES.LOLLIPOP);
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

    private void allowScreenCapture()  {
        UiDevice uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiSelector acceptButtonSelector = new UiSelector().resourceId(START_CAPTURE_BUTTON_ID);
        UiObject acceptButton = uiDevice.findObject(acceptButtonSelector);
        try {
            if (acceptButton.waitForExists(PERMISSIONS_DIALOG_DELAY_MS)) {
                assertTrue(acceptButton.click());
            }
        } catch (UiObjectNotFoundException e) {
            fail(e.getMessage());
        }
        assertTrue(screenCapturerActivity.waitForPermissionGranted());
    }
}
