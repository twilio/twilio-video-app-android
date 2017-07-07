package com.twilio.video;

import android.hardware.Camera;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class CameraCapturerTest extends BaseCameraCapturerTest {
    /*
     * The camera freeze timeout in WebRTC is 4000 MS. Added 500 MS buffer to prevent
     * false failures.
     */
    private static final int CAMERA_FREEZE_TIMEOUT_MS = 4500;

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldAllowNullListener() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA, null);
    }

    @Test
    public void shouldAllowSubsequentInstances() throws InterruptedException {
        int numInstances = 4;
        final CountDownLatch completed = new CountDownLatch(numInstances);
        for (int i = 0 ; i < numInstances ; i++) {
            final CountDownLatch firstFrameReceived = new CountDownLatch(1);
            CameraCapturer cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                    CameraCapturer.CameraSource.FRONT_CAMERA,
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
            LocalVideoTrack localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity,
                    true, cameraCapturer);

            // Validate we got our first frame
            assertTrue(firstFrameReceived.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

            localVideoTrack.release();
            completed.countDown();
        }
        assertTrue(completed.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldAllowSettingFpsVideoConstraints() throws InterruptedException {
        final CountDownLatch firstFrameReceived = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA,
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
        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minFps(5)
                .maxFps(15)
                .build();
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity,
                true,
                cameraCapturer,
                videoConstraints);

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Validate our constraints are applied
        assertEquals(videoConstraints, localVideoTrack.getVideoConstraints());
    }

    @Test
    public void shouldCreateLocalVideoTrackIfVideoConstraintsCompatible() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA, null);

        VideoConstraints videoConstraints = new VideoConstraints.Builder()
                .minFps(0)
                .maxFps(30)
                .minVideoDimensions(VideoDimensions.WVGA_VIDEO_DIMENSIONS)
                .maxVideoDimensions(VideoDimensions.HD_540P_VIDEO_DIMENSIONS)
                .aspectRatio(VideoConstraints.ASPECT_RATIO_16_9)
                .build();

        assumeTrue(LocalVideoTrack.constraintsCompatible(cameraCapturer, videoConstraints));

        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity,
                true,
                cameraCapturer,
                videoConstraints);
        assertNotNull(localVideoTrack);
        localVideoTrack.release();
    }

    @Test
    public void shouldAllowCameraSwitch() throws InterruptedException {
        final CountDownLatch cameraSwitched = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {

                    }

                    @Override
                    public void onCameraSwitched() {
                        cameraSwitched.countDown();
                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Add renderer
        localVideoTrack.addRenderer(frameCountRenderer);

        // Validate we get a frame
        assertTrue(frameCountRenderer.waitForFrame(CAMERA_CAPTURE_DELAY_MS));

        // Validate front camera source
        assertEquals(CameraCapturer.CameraSource.FRONT_CAMERA,
                cameraCapturer.getCameraSource());

        // Perform camera switch
        cameraCapturer.switchCamera();

        // Validate our switch happened
        assertTrue(cameraSwitched.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Validate we get a frame after camera switch
        assertTrue(frameCountRenderer.waitForFrame(CAMERA_CAPTURE_DELAY_MS));

        // Validate back camera source
        assertEquals(CameraCapturer.CameraSource.BACK_CAMERA,
                cameraCapturer.getCameraSource());
    }

    @Test
    public void shouldAllowCameraSwitchWhileNotOnLocalVideo() throws InterruptedException {
        final CountDownLatch cameraSwitched = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {

                    }

                    @Override
                    public void onCameraSwitched() {
                        cameraSwitched.countDown();
                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });

        // Switch our camera
        cameraCapturer.switchCamera();

        // Now add our video track
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Validate our switch happened
        assertTrue(cameraSwitched.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Add renderer
        localVideoTrack.addRenderer(frameCountRenderer);

        // Validate we get a frame
        assertTrue(frameCountRenderer.waitForFrame(CAMERA_CAPTURE_DELAY_MS));

        // Validate we are on back camera source
        assertEquals(CameraCapturer.CameraSource.BACK_CAMERA,
                cameraCapturer.getCameraSource());
    }

    @Test
    public void switchCamera_shouldFailWithSwitchPending() throws InterruptedException {
        final CountDownLatch cameraSwitchError = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                    }

                    @Override
                    public void onCameraSwitched() {
                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {
                        assertEquals(CameraCapturer.ERROR_CAMERA_SWITCH_FAILED, errorCode);
                        cameraSwitchError.countDown();
                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Switch our cameras quickly
        cameraCapturer.switchCamera();
        cameraCapturer.switchCamera();

        // Wait for callback
        assertTrue(cameraSwitchError.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldAllowUpdatingCameraParametersBeforeCapturing() throws InterruptedException {
        CountDownLatch cameraParametersSet = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);

        // Set our camera parameters
        scheduleCameraParameterFlashModeUpdate(cameraParametersSet, expectedFlashMode,
                actualCameraParameters);

        // Now add our video track
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for parameters to be set
        assertTrue(cameraParametersSet.await(10, TimeUnit.SECONDS));

        // Assume that flash is supported
        assumeNotNull(actualCameraParameters.get().getFlashMode());

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void shouldAllowUpdatingCameraParametersWhileCapturing() throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameAvailable.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });

        // Begin capturing
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for first frame
        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Schedule camera parameter update
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Assume that flash is supported
        assumeNotNull(actualCameraParameters.get().getFlashMode());

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void updateCameraParameters_shouldNotCauseCameraFreeze() throws InterruptedException {
        CountDownLatch cameraParametersSet = new CountDownLatch(1);
        final CountDownLatch cameraFroze = new CountDownLatch(1);
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameAvailable.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {
                        if (errorCode == CameraCapturer.ERROR_CAMERA_FREEZE) {
                            cameraFroze.countDown();

                        }
                    }
                });

        // Begin capturing
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for first frame
        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Schedule camera parameter update
        scheduleCameraParameterFlashModeUpdate(cameraParametersSet, expectedFlashMode,
                actualCameraParameters);

        // Wait for parameters to be set
        assertTrue(cameraParametersSet.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Assume that flash is supported
        assumeNotNull(actualCameraParameters.get().getFlashMode());

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());

        // Validate we do not get a camera freeze
        assertFalse(cameraFroze.await(CAMERA_FREEZE_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void updateCameraParameters_shouldManifestAfterCaptureCycle()
            throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameAvailable.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });

        // Begin capturing and validate our flash mode is set
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for first frame
        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Assume that flash is supported
        assumeNotNull(actualCameraParameters.get().getFlashMode());

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());

        // Release the video track
        localVideoTrack.release();

        // Set our flash mode to something else
        cameraParametersUpdated = new CountDownLatch(1);
        expectedFlashMode = Camera.Parameters.FLASH_MODE_ON;
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Recreate track
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Validate our flash mode is actually different
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void updateCameraParameters_shouldReturnFalseIfUpdateIsPending()
            throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameAvailable.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for first frame to be available
        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        // Schedule our camera parameter update
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        /*
         * Not every parameter update fails so we string together a few calls and ensure that
         * one of them fails to validate the scenario
         */
        boolean parameterUpdateScheduled = true;
        final int updateIterations = 4;
        for (int i = 0 ; i < updateIterations ; i++) {
            parameterUpdateScheduled &=
                    cameraCapturer.updateCameraParameters(new CameraParameterUpdater() {
                        @Override
                        public void apply(Camera.Parameters cameraParameters) {}
                    });
        }

        // With update pending this should have failed
        assertFalse(parameterUpdateScheduled);

        // Wait for original parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Assume that flash is supported
        assumeNotNull(actualCameraParameters.get().getFlashMode());

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void takePicture_shouldFailWithPicturePending() throws  InterruptedException{
        final CountDownLatch onPictureTakenLatch = new CountDownLatch(1);
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA,
                new CameraCapturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {
                        firstFrameAvailable.countDown();
                    }

                    @Override
                    public void onCameraSwitched() {

                    }

                    @Override
                    public void onError(@CameraCapturer.Error int errorCode) {

                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);
        CameraCapturer.PictureListener pictureListener = new CameraCapturer.PictureListener() {
            @Override
            public void onShutter() {

            }

            @Override
            public void onPictureTaken(byte[] pictureData) {
                onPictureTakenLatch.countDown();
            }
        };

        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.SECONDS));
        assertTrue(cameraCapturer.takePicture(pictureListener));
        assertFalse(cameraCapturer.takePicture(pictureListener));
        assertTrue(onPictureTakenLatch.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.SECONDS));
    }

    @Test
    public void shouldInvokePictureListenerOnCallingThread() throws InterruptedException {
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        final CountDownLatch shutterCallback = new CountDownLatch(1);
        final CountDownLatch pictureTaken = new CountDownLatch(1);

        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA, new CameraCapturer.Listener() {
            @Override
            public void onFirstFrameAvailable() {
                firstFrameAvailable.countDown();
            }

            @Override
            public void onCameraSwitched() {

            }

            @Override
            public void onError(@CameraCapturer.Error int errorCode) {

            }
        });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Wait for capturer to actually start
        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));

        /*
         * Run on UI thread to avoid thread hopping between the test runner thread and the UI
         * thread.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final long callingThreadId = Thread.currentThread().getId();
                CameraCapturer.PictureListener pictureListener =
                        new CameraCapturer.PictureListener() {
                            @Override
                            public void onShutter() {
                                assertEquals(callingThreadId, Thread.currentThread().getId());
                                shutterCallback.countDown();
                            }

                            @Override
                            public void onPictureTaken(byte[] pictureData) {
                                assertEquals(callingThreadId, Thread.currentThread().getId());
                                pictureTaken.countDown();
                            }
                        };
                assertTrue(cameraCapturer.takePicture(pictureListener));
            }
        });

        assertTrue(shutterCallback.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
        assertTrue(pictureTaken.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    private void scheduleCameraParameterFlashModeUpdate(final CountDownLatch cameraParametersSet,
                                                        final String expectedFlashMode,
                                                        final AtomicReference<Camera.Parameters> actualCameraParameters) {
        boolean parameterUpdateScheduled = cameraCapturer
                .updateCameraParameters(new CameraParameterUpdater() {
                    @Override
                    public void apply(Camera.Parameters cameraParameters) {
                        // Turn the flash only if supported
                        if (cameraParameters.getFlashMode() != null) {
                            cameraParameters.setFlashMode(expectedFlashMode);
                        }
                        actualCameraParameters.set(cameraParameters);

                        // Continue test
                        cameraParametersSet.countDown();
                    }
                });

        assertTrue(parameterUpdateScheduled);
    }
}
