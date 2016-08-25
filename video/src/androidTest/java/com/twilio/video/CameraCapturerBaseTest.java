package com.twilio.video;

import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void shouldAllowCameraSwitch() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity,
                CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA, null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait a second
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Validate our frame count is atleast 5
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount >= 5);

        // Validate front camera source
        assertEquals(CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA,
                cameraCapturer.getCameraSource());

        // Perform camera switch
        cameraCapturer.switchCamera();

        // Wait and validate our frame count is atleast 10
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount >= 10);

        // Validate back camera source
        assertEquals(CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA,
                cameraCapturer.getCameraSource());
    }
}
