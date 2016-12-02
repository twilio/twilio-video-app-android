package com.twilio.video;

import android.graphics.BitmapFactory;
import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.twilio.video.test.R;
import com.twilio.video.util.FrameCountRenderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class CameraCapturerSourceParameterizedTest extends BaseCameraCapturerTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {CameraCapturer.CameraSource.FRONT_CAMERA},
                {CameraCapturer.CameraSource.BACK_CAMERA}});
    }

    private final CameraCapturer.CameraSource cameraSource;

    public CameraCapturerSourceParameterizedTest(CameraCapturer.CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Test
    public void shouldCaptureFramesWhenVideoTrackAdded() throws InterruptedException {
        final CountDownLatch firstFrameReceived = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameReceived.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(3, TimeUnit.SECONDS));

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);
    }

    @Test
    public void shouldCaptureFramesAfterPictureTaken() throws InterruptedException {
        final CountDownLatch firstFrameReceived = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameReceived.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(3, TimeUnit.SECONDS));

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Capture frame count and take picture
        frameCount = frameCountRenderer.getFrameCount();
        assertTrue(cameraCapturer.takePicture(new CameraCapturer.PictureListener() {
            @Override
            public void onShutter() {

            }

            @Override
            public void onPictureTaken(byte[] pictureData) {

            }
        }));

        // Wait some time
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        // Validate our frame count is incrementing after taking picture
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);
    }

    @Test
    public void shouldStopCapturingFramesWhenVideoTrackRemoved() throws InterruptedException {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Remove the video track and wait
        frameCount = frameCountRenderer.getFrameCount();
        localMedia.removeVideoTrack(localVideoTrack);
        Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

        /*
         * Ensure our camera capturer is no longer capturing frames with a one frame buffer in the
         * event of a race in test case
         */
        boolean framesNotRenderering = frameCount >= (frameCountRenderer.getFrameCount() - 1);
        assertTrue(framesNotRenderering);
    }

    @Test
    public void canBeRenderedToView() throws InterruptedException {
        VideoView localVideo = (VideoView) cameraCapturerActivity.findViewById(R.id.local_video);
        final CountDownLatch renderedFirstFrame = new CountDownLatch(1);
        VideoRenderer.Listener rendererListener = new VideoRenderer.Listener() {
            @Override
            public void onFirstFrame() {
                renderedFirstFrame.countDown();
            }

            @Override
            public void onFrameDimensionsChanged(int width, int height, int rotation) {

            }
        };
        localVideo.setListener(rendererListener);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        localVideoTrack.addRenderer(localVideo);

        /*
         * Validate we rendered the first frame and wait a few seconds so we can see the
         * frames if we are watching :-)
         */
        assertTrue(renderedFirstFrame.await(2, TimeUnit.SECONDS));
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        localVideoTrack.removeRenderer(localVideo);
    }

    @Test
    public void canBeReused() throws InterruptedException {
        int reuseCount = 5;

        // Reuse the same capturer while we iterate
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource);

        for (int i = 0 ; i < reuseCount ; i++) {
            FrameCountRenderer renderer = new FrameCountRenderer();
            localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
            int frameCount = renderer.getFrameCount();

            // Validate our frame count is nothing
            assertEquals(0, frameCount);

            // Add renderer and wait
            localVideoTrack.addRenderer(renderer);
            Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

            // Validate our frame count is incrementing
            assertTrue(renderer.getFrameCount() > frameCount);

            // Remove video track and wait
            frameCount = renderer.getFrameCount();
            localMedia.removeVideoTrack(localVideoTrack);
            Thread.sleep(TimeUnit.SECONDS.toMillis(CAMERA_CAPTURE_DELAY));

            boolean framesNotRenderering = frameCount >= (renderer.getFrameCount() - 1);
            assertTrue(framesNotRenderering);
        }
    }

    @Test
    public void shouldAllowTakingPictureWhileCapturing() throws InterruptedException {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        final CountDownLatch shutterCallback = new CountDownLatch(1);
        final CountDownLatch pictureTaken = new CountDownLatch(1);
        CameraCapturer.PictureListener pictureListener = new CameraCapturer.PictureListener() {
            @Override
            public void onShutter() {
                shutterCallback.countDown();
            }

            @Override
            public void onPictureTaken(byte[] pictureData) {
                // Validate our picture data
                assertNotNull(pictureData);
                assertNotNull(BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length));

                pictureTaken.countDown();
            }
        };

        assertTrue(cameraCapturer.takePicture(pictureListener));
        assertTrue(shutterCallback.await(10, TimeUnit.SECONDS));
        assertTrue(pictureTaken.await(10, TimeUnit.SECONDS));
    }
}
