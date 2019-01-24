/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video.util;

import android.os.Build;

public class DeviceUtils {
    private static final String SAMSUNG_GALAXY_S3_MODEL = "GT-I9300";
    private static final String SAMSUNG_GALAXY_S3_DEVICE = "m0";
    private static final String SAMSUNG_GALAXY_S6_DEVICE = "zeroflte";
    private static final String SAMSUNG_GALAXY_S7_DEVICE = "herolte";
    private static final String SAMSUNG_GALAXY_S7_ACTIVE_DEVICE = "hero2lte";
    private static final String HTC_NEXUS_9_DEVICE = "flounder";
    private static final String HTC_NEXUS_9_LTE_DEVICE = "flounder_lte";
    private static final String NEXUS_7_DEVICE = "flo";
    private static final String PIXEL_2 = "walleye";
    private static final String LG_G3 = "g3";
    private static final String ONE_PLUS_ONE = "A0001";

    public static boolean isSamsungGalaxyS7() {
        return Build.DEVICE.equals(SAMSUNG_GALAXY_S7_DEVICE);
    }

    public static boolean isSamsungGalaxyS7Active() {
        return Build.DEVICE.equals(SAMSUNG_GALAXY_S7_ACTIVE_DEVICE);
    }

    public static boolean isSamsungGalaxyS6() {
        return Build.DEVICE.equals(SAMSUNG_GALAXY_S6_DEVICE);
    }

    public static boolean isSamsungGalaxyS3() {
        return Build.MODEL.equals(SAMSUNG_GALAXY_S3_MODEL)
                && Build.DEVICE.equals(SAMSUNG_GALAXY_S3_DEVICE);
    }

    public static boolean isG3() {
        return Build.DEVICE.equals(LG_G3);
    }

    public static boolean isOnePlusOne() {
        return Build.DEVICE.equals(ONE_PLUS_ONE);
    }

    public static boolean isPixel2() {
        return Build.DEVICE.equals(PIXEL_2);
    }

    public static boolean isNexus9() {
        return Build.DEVICE.equals(HTC_NEXUS_9_DEVICE)
                || Build.DEVICE.equals(HTC_NEXUS_9_LTE_DEVICE);
    }

    public static boolean isNexus7() {
        return Build.DEVICE.equals(NEXUS_7_DEVICE);
    }
}
