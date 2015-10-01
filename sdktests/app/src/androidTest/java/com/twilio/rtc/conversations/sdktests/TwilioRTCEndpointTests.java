package com.twilio.rtc.conversations.sdktests;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.signal.Endpoint;
import com.twilio.signal.EndpointListener;
import com.twilio.signal.Invite;
import com.twilio.signal.TwilioRTC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
public class TwilioRTCEndpointTests {

    /*
     * TwilioRTC is a singleton and can only be initialized once. As a result we must track
     * if TwilioRTC has ever been initialized.
     */
    private static boolean INITIALIZED = false;

    private static int TIMEOUT = 10;

    @Rule
    public ActivityTestRule<TwilioRTCEndpointActivity> mActivityRule = new ActivityTestRule<>(
            TwilioRTCEndpointActivity.class);

    @Test
    public void testTwilioInitialize() {
        final CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(mActivityRule.getActivity(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void testTwilioInitializeRepeatedly() {
        int attempts = 10;
        final CountDownLatch initLatch = INITIALIZED ? new CountDownLatch(0) : new CountDownLatch(1);
        final CountDownLatch errorLatch = INITIALIZED ? new CountDownLatch(attempts) : new CountDownLatch(attempts - 1);

        for(int i = 0; i < attempts; i++) {
            TwilioRTC.initialize(mActivityRule.getActivity(), countDownInitListenerCallback(initLatch, errorLatch));
            if(!INITIALIZED) {
                INITIALIZED = true;
            }
        }

        try {
            initLatch.await(TIMEOUT, TimeUnit.SECONDS);
            errorLatch.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("test timed out after" + TIMEOUT);
        }

    }

    private TwilioRTC.InitListener countDownInitListenerCallback(final CountDownLatch initCountDownLatch, final CountDownLatch errorCountDownLatch) {
        return new TwilioRTC.InitListener() {
            @Override
            public void onInitialized() {
                initCountDownLatch.countDown();
            }

            @Override
            public void onError(Exception e) {
                org.junit.Assert.assertEquals("Twilio.initialize() already called", e.getMessage());
                errorCountDownLatch.countDown();
            }
        };
    }

    @Test
    public void testTwilioCreateNullEndpoint() {
        final CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(mActivityRule.getActivity(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);

        Endpoint endpoint = TwilioRTC.createEndpoint(null,null);
        org.junit.Assert.assertNull(endpoint);
    }

    @Test
    public void testTwilioCreateEndpoinWithNullListener() {
        final CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(mActivityRule.getActivity(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);

        Endpoint endpoint = TwilioRTC.createEndpoint(null,null);
        org.junit.Assert.assertNull(endpoint);
    }

    @Test
    public void testTwilioCreateEndpointWithInvalidToken() {
        CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(mActivityRule.getActivity(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);

        waitLatch = new CountDownLatch(1);
        Endpoint endpoint = TwilioRTC.createEndpoint("DEADBEEF", new EndpointListener() {
            @Override
            public void onStartListeningForInvites(Endpoint endpoint) {

            }

            @Override
            public void onStopListeningForInvites(Endpoint endpoint) {

            }

            @Override
            public void onFailedToStartListening(Endpoint endpoint, int i, String s) {

            }

            @Override
            public void onReceiveConversationInvite(Endpoint endpoint, Invite invite) {

            }
        });

        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);
        org.junit.Assert.assertNotNull(endpoint);
    }

    private void initialize(Context context, TwilioRTC.InitListener initListener) {
        TwilioRTC.initialize(context, initListener);
        if(!INITIALIZED) {
            INITIALIZED = true;
        }
    }

    private TwilioRTC.InitListener initListener(final CountDownLatch wait) {
        return new TwilioRTC.InitListener() {
            @Override
            public void onInitialized() {
                if(INITIALIZED) {
                    org.junit.Assert.fail("initialized but initialize was called");
                } else {
                    wait.countDown();
                }
            }

            @Override
            public void onError(Exception e) {
                if(!INITIALIZED) {
                    org.junit.Assert.fail(e.getMessage());
                } else {
                    wait.countDown();
                }
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
