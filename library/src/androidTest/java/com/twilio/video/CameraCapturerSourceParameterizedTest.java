/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.graphics.BitmapFactory;
import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseCameraCapturerTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.twilio.video.test.R;
import com.twilio.video.util.DeviceUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

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

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        assumeTrue(CameraCapturer.isSourceAvailable(cameraSource));
    }

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldCaptureFramesWhenVideoTrackCreated() throws InterruptedException {
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
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldCaptureFramesAfterPictureTaken() throws InterruptedException {
        // TODO: Frames stop after takePicture with front camera on Samsung Galaxy S3 GSDK-1110
        assumeFalse(DeviceUtils.isSamsungGalaxyS3() &&
                cameraSource == CameraCapturer.CameraSource.FRONT_CAMERA);
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
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);
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
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);
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
            LocalVideoTrack localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity,
                    true, cameraCapturer);

            // Validate we got our first frame
            assertTrue(firstFrameReceived.get().await(CAMERA_CAPTURE_DELAY_MS,
                    TimeUnit.MILLISECONDS));

            // Release video track
            localVideoTrack.release();
        }
    }

    @Test
    public void shouldAllowTakingPictureWhileCapturing() throws InterruptedException {
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        final CountDownLatch shutterCallback = new CountDownLatch(1);
        final CountDownLatch pictureTaken = new CountDownLatch(1);

        cameraCapturer = new CameraCapturer(cameraCapturerActivity,
                cameraSource,
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

        assertTrue(firstFrameAvailable.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
        assertTrue(cameraCapturer.takePicture(pictureListener));
        assertTrue(shutterCallback.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
        assertTrue(pictureTaken.await(CAMERA_CAPTURE_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldAllowTakingPictureRightBeforeCapturerStarts() throws InterruptedException {
        final CountDownLatch shutterCallback = new CountDownLatch(1);
        final CountDownLatch pictureTaken = new CountDownLatch(1);

        cameraCapturer = new CameraCapturer(cameraCapturerActivity, cameraSource);
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, cameraCapturer);

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
