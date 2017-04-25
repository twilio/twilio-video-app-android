package com.twilio.video;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.twilio.video.util.ReflectionUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScreenCapturerUnitTest {
    @Mock Context context;

    @Before
    public void setup() throws Exception {
        ReflectionUtils.setFinalStaticField(Build.VERSION.class.getField("SDK_INT"),
                Build.VERSION_CODES.LOLLIPOP);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailOnDevicesLessThanLollipop() throws Exception {
        ReflectionUtils.setFinalStaticField(Build.VERSION.class.getField("SDK_INT"),
                Build.VERSION_CODES.KITKAT);
        new ScreenCapturer(context, 2, new Intent(), null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullContext() {
        new ScreenCapturer(null, 2, new Intent(), null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullIntent() {
        new ScreenCapturer(context, 2, null, null);
    }
}
