package com.twilio.conversations.helper;

import android.content.Context;

import com.twilio.conversations.TwilioConversations;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TwilioConversationsHelper {

    public static void initialize(Context context) throws InterruptedException {
                final CountDownLatch initLatch = new CountDownLatch(1);

        TwilioConversations.initialize(context,
                new TwilioConversations.InitListener() {
                    @Override
                    public void onInitialized() {
                        initLatch.countDown();
                    }

                    @Override
                    public void onError(Exception exception) {
                        fail(exception.getMessage());
                    }
                });

        assertTrue(initLatch.await(10, TimeUnit.SECONDS));

    }

    public static void destroy() {
        if(TwilioConversations.isInitialized()) {
            TwilioConversations.destroy();
        }
        while(TwilioConversations.isInitialized());
    }

}
