package com.twilio.video.util;

import com.twilio.accessmanager.AccessManager;
import com.twilio.video.simplersignaling.SimplerSignalingUtils;
import com.twilio.video.test.BuildConfig;

import java.util.HashMap;
import java.util.Map;

public class AccessTokenUtils {
    private static final Map<String, AccessManager> tokenCache = new HashMap<>();

    public static String getAccessToken(String username) {
        if (!tokenCache.containsKey(username)) {
            String token = SimplerSignalingUtils.getAccessToken(username,
                    BuildConfig.ENVIRONMENT,
                    BuildConfig.TOPOLOGY);
            tokenCache.put(username, new AccessManager(token));

            return token;
        } else {
            AccessManager userAccessManager = tokenCache.get(username);

            if (userAccessManager.isTokenExpired()) {
                String token = SimplerSignalingUtils.getAccessToken(username,
                        BuildConfig.ENVIRONMENT,
                        BuildConfig.TOPOLOGY);
                userAccessManager.updateToken(token);
            }

            return userAccessManager.getToken();
        }
    }
}
