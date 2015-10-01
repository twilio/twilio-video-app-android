package com.twilio.rtc.conversations.noservice;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.signal.TwilioRTC;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class TwilioRTCNoServiceTest {

    private static int TIMEOUT = 10;

    @Rule
    public ActivityTestRule<TwilioRTCNoServiceActivity> mActivityRule = new ActivityTestRule<>(
            TwilioRTCNoServiceActivity.class);

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
