package com.twilio.rtc.conversations.sdktests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;

import com.twilio.signal.TwilioRTC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * JUnit4 Ui Tests for {@link TwilioActivity} using the {@link AndroidJUnitRunner}.
 * This class uses the JUnit4 syntax for tests.
 */
@RunWith(AndroidJUnit4.class)
public class TwilioActivityManifestServiceTest {

    private static int TIMEOUT = 10;

    @Rule
    public ActivityTestRule<TwilioActivity> mActivityRule = new ActivityTestRule<>(
            TwilioActivity.class);

    @Test
    public void testTwilioInitializeWithoutServiceInManifest() {
        final CountDownLatch wait = new CountDownLatch(1);

        TwilioRTC.initialize(mActivityRule.getActivity(), new TwilioRTC.InitListener() {
            @Override
            public void onInitialized() {
                org.junit.Assert.fail("twilio service missing from manifest exception did not occur");
            }

            @Override
            public void onError(Exception e) {
                org.junit.Assert.assertEquals("com.twilio.signal.TwilioRTCService is not declared in AndroidManifest.xml", e.getMessage());
                wait.countDown();
            }
        });

        try {
            wait.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("test timed out after" + TIMEOUT);
        }
    }

}
