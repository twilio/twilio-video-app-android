/*
 * Copyright (C) 2019 Twilio, Inc.
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
