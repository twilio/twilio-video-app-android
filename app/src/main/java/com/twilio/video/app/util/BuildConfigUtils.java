package com.twilio.video.app.util;

import com.twilio.video.app.BuildConfig;

public class BuildConfigUtils {
    private static final String INTERNAL_FLAVOR = "internal";

    public static boolean isInternalFlavor() {
        return BuildConfig.FLAVOR.equals(INTERNAL_FLAVOR);
    }

    public static boolean isInternalRelease() {
        return isInternalFlavor() && !BuildConfig.DEBUG;
    }
}
