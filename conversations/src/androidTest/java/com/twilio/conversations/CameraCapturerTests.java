package com.twilio.conversations;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.activity.CameraCapturerTestActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CameraCapturerTests {
    @Rule
    public ActivityTestRule<CameraCapturerTestActivity> activityRule = new ActivityTestRule<>(
            CameraCapturerTestActivity.class);
    CameraCapturerTestActivity cameraCapturerTestActivity;

    @Before
    public void setup() {
        cameraCapturerTestActivity = activityRule.getActivity();
    }

    @After
    public void teardown() {
        onView(withContentDescription(CameraCapturerTestActivity.CONTENT_DESCRIPTION_STOP_PREVIEW))
                .perform(click());
    }

    @Test
    public void startPreview_shouldReturnErrorWhenNoPreviewContainerProvided()
            throws InterruptedException {
        final CountDownLatch capturerErrorOccured = new CountDownLatch(1);
        CameraCapturer cameraCapturer = CameraCapturerFactory
                .createCameraCapturer(cameraCapturerTestActivity,
                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                        new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {
                                capturerErrorOccured.countDown();
                            }
                        });
        cameraCapturer.startPreview(null);

        assertTrue(capturerErrorOccured.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void startPreview_shouldAddNewViewWithCameraPreview() {
        onView(withContentDescription(CameraCapturerTestActivity
                .CONTENT_DESCRIPTION_CREATE_CAPTURER))
                .perform(click());
        onView(withContentDescription(CameraCapturerTestActivity.CONTENT_DESCRIPTION_START_PREVIEW))
                .perform(click());

        onView(withContentDescription(R.string.capturer_preview_content_description))
                .check(matches(isDisplayed()));
        assertTrue(cameraCapturerTestActivity.cameraCapturer.isPreviewing());
    }
}
