package com.twilio.rtc.conversations.sdktests;

import android.app.Activity;
import android.content.Context;

import com.twilio.signal.TwilioRTC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Common tools used in tests
 */
public class TestTools {

    /*
     * TwilioRTC is a singleton and can only be initialized once. As a result we must track
     * if TwilioRTC has ever been initialized.
     */
    private static boolean initialized = false;

    public static int TIMEOUT = 10;

    public static void initializeTwilioSDK(Activity activity) {
        CountDownLatch waitLatch = new CountDownLatch(1);
        initialize(activity.getApplicationContext(), initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void initialize(Context context, TwilioRTC.InitListener initListener) {
        TwilioRTC.initialize(context, initListener);
    }

    public static TwilioRTC.InitListener initListener(final CountDownLatch wait) {
        return new TwilioRTC.InitListener() {
            @Override
            public void onInitialized() {
                if(!initialized) {
                    initialized = true;
                    wait.countDown();
                } else {
                    org.junit.Assert.fail("initalized but initialize was already called");
                }
            }

            @Override
            public void onError(Exception e) {
                if(!initialized) {
                    org.junit.Assert.fail(e.getMessage());
                } else {
                    wait.countDown();
                }
            }
        };
    }

    public static TwilioRTC.InitListener countDownInitListenerCallback(final CountDownLatch initCountDownLatch, final CountDownLatch errorCountDownLatch) {
        return new TwilioRTC.InitListener() {
            @Override
            public void onInitialized() {
                initCountDownLatch.countDown();
                initialized = true;
            }

            @Override
            public void onError(Exception e) {
                org.junit.Assert.assertEquals("Twilio.initialize() already called", e.getMessage());
                errorCountDownLatch.countDown();
            }
        };
    }

    public static void wait(CountDownLatch wait, int timeout, TimeUnit timeUnit) {
        try {
            wait.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            org.junit.Assert.fail("timed out after" + TIMEOUT);
        }
    }


}
