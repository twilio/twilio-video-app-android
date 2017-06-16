package com.twilio.video.util;

import android.os.Build;

public class DeviceUtils {
    private static final String SAMSUNG_GALAXY_S3_MODEL = "GT-I9300";
    private static final String SAMSUNG_GALAXY_S3_DEVICE = "m0";
    private static final String SAMSUNG_GALAXY_S7_DEVICE = "herolte";

    public static boolean isSamsungGalaxyS7() {
        return Build.DEVICE.equals(SAMSUNG_GALAXY_S7_DEVICE);
    }

    public static boolean isSamsungGalaxyS3() {
        return Build.MODEL.equals(SAMSUNG_GALAXY_S3_MODEL) &&
                Build.DEVICE.equals(SAMSUNG_GALAXY_S3_DEVICE);
    }
}
