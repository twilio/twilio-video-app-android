package com.twilio.video.util;

import android.content.Context;

import com.twilio.common.AccessManager;
import com.twilio.video.provider.AccessTokenProvider;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AccessManagerUtils {
    /**
     * A synchronous method that returns an initialized AccessManager
     */
    public static AccessManager obtainAccessManager(Context context, String username)
            throws InterruptedException {
        String accessToken = SimplerSignalingUtils.getAccessToken(username, "prod");

        return new AccessManager(context, accessToken, null);
    }
}
