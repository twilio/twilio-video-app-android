package com.twilio.conversations.utils;

import android.content.Context;

import com.twilio.conversations.TwilioConversations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TwilioConversationsUtils {
    /**
     * TwilioConversations is a singleton and can only be initialized once.
     * As a result we must track if TwilioConversations has ever been initialized.
     */
    private static boolean initialized = false;

    public static int TIMEOUT = 10;

    public static void initializeTwilioSDK(Context applicationContext) {
        CountDownLatch waitLatch = new CountDownLatch(1);
        TwilioConversations.initialize(applicationContext, initListener(waitLatch));
        wait(waitLatch, TIMEOUT, TimeUnit.SECONDS);
    }

    public static void destroyTwilioSDK() {
        TwilioConversations.destroy();
        while (TwilioConversations.isInitialized());
        initialized = false;
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static TwilioConversations.InitListener initListener(final CountDownLatch wait) {
        return new TwilioConversations.InitListener() {
            @Override
            public void onInitialized() {
                if(!initialized) {
                    initialized = true;
                    wait.countDown();
                } else {
                    org.junit.Assert.fail("initialized but initialize was already called");
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

    public static TwilioConversations.InitListener
    countDownInitListenerCallback(final CountDownLatch initCountDownLatch,
                                  final CountDownLatch errorCountDownLatch) {
        return new TwilioConversations.InitListener() {
            @Override
            public void onInitialized() {
                initCountDownLatch.countDown();
                initialized = true;
            }

            @Override
            public void onError(Exception e) {
                org.junit.Assert.assertEquals("Initialize already called", e.getMessage());
                errorCountDownLatch.countDown();
            }
        };
    }

    public static void wait(CountDownLatch wait, int timeout, TimeUnit timeUnit) {
        try {
            if (!wait.await(timeout, timeUnit)) {
                org.junit.Assert.fail("timed out after " + TIMEOUT);
            }
        } catch (InterruptedException e) {
            org.junit.Assert.fail("Thread interrupted");
        }
    }
}
