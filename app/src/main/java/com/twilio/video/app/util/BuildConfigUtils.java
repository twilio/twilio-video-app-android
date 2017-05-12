package com.twilio.video.app.util;

import com.twilio.video.app.BuildConfig;

public class BuildConfigUtils {
    private static final String INTERNAL_FLAVOR = "internal";
    private static final String DEVELOPMENT_FLAVOR = "development";

    public static boolean isDevelopmentFlavor() {
        return BuildConfig.FLAVOR.equals(DEVELOPMENT_FLAVOR);
    }

    public static boolean isInternalFlavor() {
        return BuildConfig.FLAVOR.equals(INTERNAL_FLAVOR);
    }

    public static boolean isInternalRelease() {
        return isInternalFlavor() && !BuildConfig.DEBUG;
    }
}
