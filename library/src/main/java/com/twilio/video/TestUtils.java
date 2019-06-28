package com.twilio.video;

import android.content.Context;
import android.provider.Settings;

public class TestUtils {
    public static final long ICE_TIMEOUT = 20000;
    public static final long SIP_TIMEOUT = 125;
    public static final long STATE_TRANSITION_TIMEOUT = 20;
    public static final long INVALID_REGION_TIMEOUT = 60;
    public static final long SMALL_WAIT = 5000;

    /**
     * See
     * https://firebase.google.com/docs/test-lab/android/android-studio#modify_instrumented_test_behavior_for_testlab
     *
     * @param context
     * @return true if this code is running inside of Firebase Test Lab
     */
    static boolean isFTL(Context context) {
        String testLabSetting =
                Settings.System.getString(context.getContentResolver(), "firebase.test.lab");
        return "true".equals(testLabSetting);
    }

    static void blockingWait(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
