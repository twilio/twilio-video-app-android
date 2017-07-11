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
    private static final String SAMSUNG_GALAXY_S7_DEVICE = "herolte";

    public static boolean isSamsungGalaxyS7() {
        return Build.DEVICE.equals(SAMSUNG_GALAXY_S7_DEVICE);
    }

    public static boolean isSamsungGalaxyS3() {
        return Build.MODEL.equals(SAMSUNG_GALAXY_S3_MODEL) &&
                Build.DEVICE.equals(SAMSUNG_GALAXY_S3_DEVICE);
    }
}
