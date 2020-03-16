/*
 * Copyright (C) 2018 Twilio, Inc.
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

package com.twilio.video;

import android.content.Context;
import android.provider.Settings;

public class TestUtils {
    public static final long ONE_SECOND = 1000;
    public static final long TWO_SECONDS = 2000;
    public static final long THREE_SECONDS = 3000;
    public static final long FOUR_SECONDS = 4000;
    public static final long FIVE_SECONDS = 5000;

    public static final long SMALL_WAIT = 5000;
    public static final long ICE_TIMEOUT = 30000;
    public static final long SIP_TIMEOUT = 125;
    public static final long STATE_TRANSITION_TIMEOUT = 15;
    public static final long INVALID_REGION_TIMEOUT = 60;

    /**
     * See
     * https://firebase.google.com/docs/test-lab/android/android-studio#modify_instrumented_test_behavior_for_testlab
     *
     * @param context
     * @return true if this code is running inside of Firebase Test Lab
     */
    public static boolean isFTL(Context context) {
        String testLabSetting =
                Settings.System.getString(context.getContentResolver(), "firebase.test.lab");
        return "true".equals(testLabSetting);
    }

    public static void blockingWait(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
