package com.twilio.video;

import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CameraCapturerBaseTest extends BaseCameraCapturerTest {
    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullContext() {
        cameraCapturer = CameraCapturer.create(null,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null);
    }

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullSource() {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, null, null);
    }
}
