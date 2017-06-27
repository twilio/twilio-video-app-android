package com.twilio.video;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Camera2CapturerTest {

    /*
     * Validates that isSupported can be invoked on all API levels without resulting in a runtime
     * exception. See https://code.google.com/p/android/issues/detail?id=209129.
     */
    @Test
    public void shouldAllowCompatibilityCheck() {
        Camera2Capturer.isSupported(InstrumentationRegistry.getContext());
    }
}
