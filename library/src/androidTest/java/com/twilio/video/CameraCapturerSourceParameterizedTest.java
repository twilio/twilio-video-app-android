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
import java.util.concurrent.atomic.AtomicReference;

import com.twilio.video.test.R;

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

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldCaptureFramesAfterPictureTaken() throws InterruptedException {
        final CountDownLatch firstFrameReceived = new CountDownLatch(1);
        final CountDownLatch pictureTaken = new CountDownLatch(1);
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
        assertTrue(firstFrameReceived.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Add renderer
        localVideoTrack.addRenderer(frameCountRenderer);

        // Capture frame count and take picture
        assertTrue(cameraCapturer.takePicture(new CameraCapturer.PictureListener() {
            @Override
            public void onShutter() {

            }

            @Override
            public void onPictureTaken(byte[] pictureData) {
                pictureTaken.countDown();
            }
        }));

        // Wait for picture taken
        assertTrue(pictureTaken.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Validate we receive a new frame
        assertTrue(frameCountRenderer.waitForFrame(CAMERA_CAPTURE_DELAY_MS));
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
        assertTrue(renderedFirstFrame.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
        localVideoTrack.removeRenderer(localVideo);
    }

    @Test
    public void canBeReused() throws InterruptedException {
        int reuseCount = 2;

        // Reuse the same capturer while we iterate
        final AtomicReference<CountDownLatch> firstFrameReceived = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameReceived.get().countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });
        for (int i = 0 ; i < reuseCount ; i++) {
            firstFrameReceived.set(new CountDownLatch(1));
            localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);

            // Validate we got our first frame
            assertTrue(firstFrameReceived.get().await(CAMERA_CAPTURE_DELAY_MS,
                    TimeUnit.MILLISECONDS));

            // Remove video track
            localMedia.removeVideoTrack(localVideoTrack);
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
        assertTrue(shutterCallback.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
        assertTrue(pictureTaken.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
    }
}
