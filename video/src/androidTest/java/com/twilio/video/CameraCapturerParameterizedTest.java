package com.twilio.video;

import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class CameraCapturerParameterizedTest extends BaseCameraCapturerTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {CameraCapturer.CameraSource.CAMERA_SOURCE_FRONT_CAMERA},
                {CameraCapturer.CameraSource.CAMERA_SOURCE_BACK_CAMERA}});
    }

    private final CameraCapturer.CameraSource cameraSource;

    public CameraCapturerParameterizedTest(CameraCapturer.CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Test
    public void shouldCaptureFramesWhenAddedToVideoTrack() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, cameraSource, null);
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
    }

    @Test
    public void shouldStopCapturingFramesWhenRemovedFromVideoTrack() throws InterruptedException {
        cameraCapturer = CameraCapturer.create(cameraCapturerActivity, cameraSource, null);
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

        // Remove the renderer and wait
        frameCount = frameCountRenderer.getFrameCount();
        localVideoTrack.removeRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));

        // Ensure our camera capturer is no longer capturing frames
        assertEquals(frameCount, frameCountRenderer.getFrameCount());
    }
}
