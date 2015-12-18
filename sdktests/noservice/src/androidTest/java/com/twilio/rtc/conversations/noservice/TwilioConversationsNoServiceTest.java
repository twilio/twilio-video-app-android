package com.twilio.rtc.conversations.noservice;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.TwilioConversations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class TwilioConversationsNoServiceTest {

    private static int TIMEOUT = 10;

    @Rule
    public ActivityTestRule<TwilioConversationsNoServiceActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsNoServiceActivity.class);

    @Test
    public void testTwilioInitializeWithoutServiceInManifest() {
        final CountDownLatch wait = new CountDownLatch(1);

        /*
         * NOTE: The service has been removed from the SDK. When the service is added back this test
         * must be refactored.
         */
        TwilioConversations.initialize(mActivityRule.getActivity(), new TwilioConversations.InitListener() {
            @Override
            public void onInitialized() {
                wait.countDown();
            }

            @Override
            public void onError(Exception e) {
                org.junit.Assert.fail("twilio initialization failed");
            }
        });

        try {
            wait.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("test timed out after" + TIMEOUT);
        }
    }

}
