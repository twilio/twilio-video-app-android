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
        cameraCapturer = new CameraCapturer(null,
                CameraCapturer.CameraSource.FRONT_CAMERA,
                null);
    }

    @Test(expected = NullPointerException.class)
    public void create_shouldFailWithNullSource() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, null, null);
    }

    @Test
    public void shouldAllowCameraSwitch() throws InterruptedException {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA,
                null);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Validate front camera source
        assertEquals(CameraCapturer.CameraSource.FRONT_CAMERA,
                cameraCapturer.getCameraSource());

        // Perform camera switch
        cameraCapturer.switchCamera();

        // Wait and validate our frame count is still incrementing
        frameCount = frameCountRenderer.getFrameCount();
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Validate back camera source
        assertEquals(CameraCapturer.CameraSource.BACK_CAMERA,
                cameraCapturer.getCameraSource());
    }

    @Test
    public void shouldAllowCameraSwitchWhileNotOnLocalVideo() throws InterruptedException {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA,
                null);

        // Switch our camera
        cameraCapturer.switchCamera();

        // Now add our video track
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Validate we are on back camera source
        assertEquals(CameraCapturer.CameraSource.BACK_CAMERA,
                cameraCapturer.getCameraSource());
    }
}
