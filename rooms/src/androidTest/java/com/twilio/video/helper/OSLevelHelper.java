package com.twilio.video.helper;

import android.os.Build;

public class OSLevelHelper {

    /**
     * Runtime permissions to enable the camera are not enabled in this test
     */
    public static boolean requiresRuntimePermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
