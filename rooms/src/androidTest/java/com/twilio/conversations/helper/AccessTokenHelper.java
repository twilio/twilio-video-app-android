package com.twilio.conversations.helper;

import android.content.Context;

import com.twilio.common.AccessManager;
import com.twilio.conversations.provider.AccessTokenProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AccessTokenHelper {
    /**
     * A synchronous method that returns an initialized TwilioAccessManager
     */
    public static AccessManager obtainAccessManager(Context context, String username) throws InterruptedException {
        String accessToken = obtainCapabilityToken(username);
        final CountDownLatch tokenUpdatedLatch = new CountDownLatch(1);
        AccessManager twilioAccessManager = new AccessManager(context, accessToken,
                new AccessManager.Listener() {
                    @Override
                    public void onTokenExpired(AccessManager twilioAccessManager) {
                        fail();
                    }

                    @Override
                    public void onTokenUpdated(AccessManager twilioAccessManager) {
                        tokenUpdatedLatch.countDown();
                    }

                    @Override
                    public void onError(AccessManager twilioAccessManager, String s) {
                        fail(s);
                    }
                });

        assertTrue(tokenUpdatedLatch.await(10, TimeUnit.SECONDS));

        return twilioAccessManager;
    }

    /**
     * A synchronous method that returns an access token
     */
    private static String obtainCapabilityToken(String username) throws InterruptedException {

        final String[] capabilityToken = new String[1];

        final CountDownLatch tokenLatch = new CountDownLatch(1);
        AccessTokenProvider.obtainTwilioCapabilityToken(username, new Callback<String>() {
            @Override
            public void success(String s, Response response) {
                capabilityToken[0] = s;
                tokenLatch.countDown();
            }

            @Override
            public void failure(RetrofitError error) {
                fail(error.getMessage());
            }
        });

        assertTrue(tokenLatch.await(10, TimeUnit.SECONDS));

        return capabilityToken[0];
    }
}
