package com.twilio.rtc.conversations.loglevel;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.conversations.TwilioConversations;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


/*
 * Adding other tests to this test module that call initialize may invalidate this test since TwilioConversations
 * is a singleton and can only be initialized once.
 */
@RunWith(AndroidJUnit4.class)
public class TwilioConversationsLogLevelTests {

    private static int TIMEOUT = 10;

    @Rule
    public ActivityTestRule<TwilioConversationsLogLevelActivity> mActivityRule = new ActivityTestRule<>(
            TwilioConversationsLogLevelActivity.class);

    @Test
    public void canSetLogLevelBeforeAndAfterInit() {
        int level = TwilioConversations.LogLevel.VERBOSE;
        TwilioConversations.setLogLevel(level);
        assertEquals(level, TwilioConversations.getLogLevel());

        final CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(mActivityRule.getActivity(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);

        assertEquals(level, TwilioConversations.getLogLevel());
    }

    private void initialize(Context context, TwilioConversations.InitListener initListener) {
        TwilioConversations.initialize(context, initListener);
    }

    private TwilioConversations.InitListener initListener(final CountDownLatch wait) {
        return new TwilioConversations.InitListener() {
            @Override
            public void onInitialized() {
                wait.countDown();
            }

            @Override
            public void onError(Exception e) {
                fail(e.getMessage());
            }
        };
    }

    private void wait(CountDownLatch wait, int timeout, TimeUnit timeUnit) {
        try {
            wait.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            fail("timed out after" + TIMEOUT);
        }
    }

}
