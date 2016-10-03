package com.twilio.video.app.util;

import android.content.Context;

import com.twilio.common.AccessManager;

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
