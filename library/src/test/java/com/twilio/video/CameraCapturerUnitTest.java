package com.twilio.video;

import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CameraCapturerUnitTest {
    @Mock Context context;

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullContext() {
        new CameraCapturer(null, CameraCapturer.CameraSource.FRONT_CAMERA);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullSource() {
        new CameraCapturer(context, null);
    }
}
