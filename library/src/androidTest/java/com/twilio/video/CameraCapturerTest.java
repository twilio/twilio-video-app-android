package com.twilio.video;

import android.hardware.Camera;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

@RunWith(AndroidJUnit4.class)
public class CameraCapturerTest extends BaseCameraCapturerTest {
    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullContext() {
        cameraCapturer = new CameraCapturer(null, CameraCapturer.CameraSource.FRONT_CAMERA);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullSource() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity, null);
    }

    @Test
    public void shouldAllowNullListener() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.FRONT_CAMERA, null);
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
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(CAMERA_CAPTURE_DELAY_MS);

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

        // Validate front camera source
        assertEquals(CameraCapturer.CameraSource.FRONT_CAMERA,
                cameraCapturer.getCameraSource());

        // Perform camera switch
        cameraCapturer.switchCamera();

        // Validate our switch happened
        assertTrue(cameraSwitched.await(10, TimeUnit.SECONDS));

        // Wait and validate our frame count is still incrementing
        frameCount = frameCountRenderer.getFrameCount();
        Thread.sleep(CAMERA_CAPTURE_DELAY_MS);
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

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
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        int frameCount = frameCountRenderer.getFrameCount();

        // Validate our switch happened
        assertTrue(cameraSwitched.await(10, TimeUnit.SECONDS));

        // Validate our frame count is nothing
        assertEquals(0, frameCount);

        // Add renderer and wait
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(CAMERA_CAPTURE_DELAY_MS);

        // Validate our frame count is incrementing
        assertTrue(frameCountRenderer.getFrameCount() > frameCount);

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
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);

        // Switch our cameras quickly
        cameraCapturer.switchCamera();
        cameraCapturer.switchCamera();

        // Wait for callback
        assertTrue(cameraSwitchError.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void shouldAllowUpdatingCameraParametersBeforeCapturing() throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);

        // Set our camera parameters
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Now add our video track
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void shouldAllowUpdatingCameraParametersWhileCapturing() throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);

        // Begin capturing
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);

        // Schedule camera parameter update
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void updateCameraParameters_shouldManifestAfterCaptureCycle()
            throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);

        // Begin capturing and validate our flash mode is set
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());

        // Remove the video track
        localMedia.removeVideoTrack(localVideoTrack);

        // Set our flash mode to something else
        cameraParametersUpdated = new CountDownLatch(1);
        expectedFlashMode = Camera.Parameters.FLASH_MODE_ON;
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode, actualCameraParameters);

        // Re add the track
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);

        // Wait for parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Validate our flash mode is actually different
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void updateCameraParameters_shouldReturnFalseIfUpdateIsPending()
            throws InterruptedException {
        CountDownLatch cameraParametersUpdated = new CountDownLatch(1);
        String expectedFlashMode = Camera.Parameters.FLASH_MODE_TORCH;
        AtomicReference<Camera.Parameters> actualCameraParameters = new AtomicReference<>();
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);

        // Schedule our camera parameter update
        scheduleCameraParameterFlashModeUpdate(cameraParametersUpdated, expectedFlashMode,
                actualCameraParameters);

        // Immediately schedule another
        boolean parameterUpdateScheduled = cameraCapturer
                .updateCameraParameters(new CameraParameterUpdater() {
                    @Override
                    public void apply(Camera.Parameters cameraParameters) {}
                });

        // With update pending this should have failed
        assertFalse(parameterUpdateScheduled);

        // Wait for original parameters to be set
        assertTrue(cameraParametersUpdated.await(10, TimeUnit.SECONDS));

        // Validate our flash mode
        assertEquals(expectedFlashMode, actualCameraParameters.get().getFlashMode());
    }

    @Test
    public void takePicture_shouldFailIfCapturerNotRunning() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);

        assertFalse(cameraCapturer.takePicture(new CameraCapturer.PictureListener() {
            @Override
            public void onShutter() {
                fail();
            }

            @Override
            public void onPictureTaken(byte[] pictureData) {
                fail();
            }
        }));
    }

    @Test
    public void takePicture_shouldFailWithPicturePending() {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        CameraCapturer.PictureListener pictureListener = new CameraCapturer.PictureListener() {
            @Override
            public void onShutter() {

            }

            @Override
            public void onPictureTaken(byte[] pictureData) {

            }
        };

        assertTrue(cameraCapturer.takePicture(pictureListener));
        assertFalse(cameraCapturer.takePicture(pictureListener));
    }

    @Test
    public void shouldInvokePictureListenerOnCallingThread() throws InterruptedException {
        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                CameraCapturer.CameraSource.BACK_CAMERA);
        localVideoTrack = localMedia.addVideoTrack(true, cameraCapturer);
        final CountDownLatch shutterCallback = new CountDownLatch(1);
        final CountDownLatch pictureTaken = new CountDownLatch(1);

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

        assertTrue(shutterCallback.await(10, TimeUnit.SECONDS));
        assertTrue(pictureTaken.await(10, TimeUnit.SECONDS));
    }

    private void scheduleCameraParameterFlashModeUpdate(final CountDownLatch cameraParametersUpdated,
                                                        final String expectedFlashMode,
                                                        final AtomicReference<Camera.Parameters> actualCameraParameters) {
        boolean parameterUpdateScheduled = cameraCapturer
                .updateCameraParameters(new CameraParameterUpdater() {
                    @Override
                    public void apply(Camera.Parameters cameraParameters) {
                        // This lets assume we can actually support flash mode
                        assumeNotNull(cameraParameters.getFlashMode());

                        // Turn the flash on set our parameters later for validation
                        cameraParameters.setFlashMode(expectedFlashMode);
                        actualCameraParameters.set(cameraParameters);

                        // Continue test
                        cameraParametersUpdated.countDown();
                    }
                });

        assertTrue(parameterUpdateScheduled);
    }
}
