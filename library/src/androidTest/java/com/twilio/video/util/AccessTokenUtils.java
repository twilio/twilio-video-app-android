package com.twilio.video.util;

import com.twilio.video.test.BuildConfig;

public class AccessTokenUtils {

    public static String getAccessToken(String username) {
        return com.twilio.video.simplersignaling.SimplerSignalingUtils
                .getAccessToken(username, BuildConfig.ENVIRONMENT, BuildConfig.TOPOLOGY);
    }

}
