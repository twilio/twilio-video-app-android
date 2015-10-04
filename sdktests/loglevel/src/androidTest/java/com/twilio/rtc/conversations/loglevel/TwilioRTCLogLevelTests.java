package com.twilio.rtc.conversations.loglevel;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.signal.TwilioRTC;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/*
 * Adding other tests to this test module that call initialize may invalidate this test since TwilioRTC
 * is a singleton and can only be initialized once.
 */
@RunWith(AndroidJUnit4.class)
public class TwilioRTCLogLevelTests {

    private static int TIMEOUT = 10;

    @Rule
    public ActivityTestRule<TwilioRTCLogLevelActivity> mActivityRule = new ActivityTestRule<>(
            TwilioRTCLogLevelActivity.class);

    @Test
    public void testTwilioEnsureLogLevelSetBeforeAndAfterInit() {
        int level = TwilioRTC.LogLevel.VERBOSE;
        TwilioRTC.setLogLevel(level);
        org.junit.Assert.assertEquals(level, TwilioRTC.getLogLevel());

        final CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(mActivityRule.getActivity(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);

        org.junit.Assert.assertEquals(level, TwilioRTC.getLogLevel());
    }

    private void initialize(Context context, TwilioRTC.InitListener initListener) {
        TwilioRTC.initialize(context, initListener);
    }

    private TwilioRTC.InitListener initListener(final CountDownLatch wait) {
        return new TwilioRTC.InitListener() {
            @Override
            public void onInitialized() {
                wait.countDown();
            }

            @Override
            public void onError(Exception e) {
                org.junit.Assert.fail(e.getMessage());
            }
        };
    }

    private void wait(CountDownLatch wait, int timeout, TimeUnit timeUnit) {
        try {
            wait.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("timed out after" + TIMEOUT);
        }
    }

}
