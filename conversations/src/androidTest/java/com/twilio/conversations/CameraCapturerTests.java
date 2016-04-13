package com.twilio.conversations;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.activity.TwilioConversationsActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CameraCapturerTests {
    @Rule
    public ActivityTestRule<TwilioConversationsActivity> activityRule = new ActivityTestRule<>(
            TwilioConversationsActivity.class);

    @Test
    public void startPreview_shouldReturnErrorWhenNoPreviewContainerProvided()
            throws InterruptedException {
        final CountDownLatch capturerErrorOccured = new CountDownLatch(1);
        CameraCapturer cameraCapturer = CameraCapturerFactory
                .createCameraCapturer(activityRule.getActivity(),
                        CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                        new CapturerErrorListener() {
                            @Override
                            public void onError(CapturerException e) {
                                capturerErrorOccured.countDown();
                            }
                        });
        cameraCapturer.startPreview();

        assertTrue(capturerErrorOccured.await(10, TimeUnit.SECONDS));
    }
}
