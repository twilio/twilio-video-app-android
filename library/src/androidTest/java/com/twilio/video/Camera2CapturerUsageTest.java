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

import android.annotation.TargetApi;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseCamera2CapturerTest;
import com.twilio.video.util.DeviceUtils;
import com.twilio.video.util.FrameCountRenderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@TargetApi(21)
@RunWith(AndroidJUnit4.class)
public class Camera2CapturerUsageTest extends BaseCamera2CapturerTest {
    private String[] cameraIds;

    @Before
    public void setup() {
        super.setup();
        CameraManager cameraManager = (CameraManager)
                cameraCapturerActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraIds = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            fail(e.getMessage());
        }
    }

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    @Ignore("Investigate test issues on certain devices GSDK-1207")
    public void shouldAllowSimultaneousUseOfInstancesWithDifferentIds()
            throws InterruptedException {
        assumeTrue(cameraIds.length > 1);
        final String firstCameraId = cameraIds[0];
        final String secondCameraId = cameraIds[1];
        final FrameCountRenderer secondFrameCountRenderer = new FrameCountRenderer();
        final CountDownLatch firstFrameReceived = new CountDownLatch(2);
        final Camera2Capturer.Listener listener = new Camera2Capturer.Listener() {
            @Override
            public void onFirstFrameAvailable() {
                firstFrameReceived.countDown();
            }

            @Override
            public void onCameraSwitched(String newCameraId) {}

            @Override
            public void onError(Camera2Capturer.Exception camera2CapturerException) {}
        };
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity, firstCameraId, listener);
        Camera2Capturer secondCamera2Capturer = new Camera2Capturer(cameraCapturerActivity,
                secondCameraId, listener);
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);
        LocalVideoTrack secondLocalVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true,
                secondCamera2Capturer);

        // Validate frames receive by both capturers
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(CAMERA2_CAPTURER_DELAY_MS));
        secondLocalVideoTrack.addRenderer(secondFrameCountRenderer);
        assertTrue(secondFrameCountRenderer.waitForFrame(CAMERA2_CAPTURER_DELAY_MS));
        assertTrue(firstFrameReceived.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));

        // Release tracks
        localVideoTrack.release();
        secondLocalVideoTrack.release();
    }

    @Test
    public void shouldAllowCameraSwitch() throws InterruptedException {
        assumeTrue(cameraIds.length > 1);
        final String cameraId = cameraIds[0];
        final String expectedCameraId = cameraIds[1];
        final CountDownLatch cameraSwitched = new CountDownLatch(1);
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity,
                cameraId,
                new Camera2Capturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {

                    }

                    @Override
                    public void onCameraSwitched(String newCameraId) {
                        assertEquals(expectedCameraId, newCameraId);
                        assertEquals(expectedCameraId, camera2Capturer.getCameraId());
                        cameraSwitched.countDown();
                    }

                    @Override
                    public void onError(Camera2Capturer.Exception exception) {

                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);

        // Add renderer
        localVideoTrack.addRenderer(frameCountRenderer);

        // Validate we get a frame
        assertTrue(frameCountRenderer.waitForFrame(CAMERA2_CAPTURER_DELAY_MS));

        // Validate front camera source
        assertEquals(cameraId, camera2Capturer.getCameraId());

        // Perform camera switch
        camera2Capturer.switchCamera(expectedCameraId);

        // Validate our switch happened
        assertTrue(cameraSwitched.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));

        // Validate we get a frame after camera switch
        assertTrue(frameCountRenderer.waitForFrame(CAMERA2_CAPTURER_DELAY_MS));
    }

    @Test
    public void shouldAllowCameraSwitchWhileNotCapturing() throws InterruptedException {
        assumeTrue(cameraIds.length > 1);
        final String cameraId = cameraIds[0];
        final String expectedCameraId = cameraIds[1];
        final CountDownLatch cameraSwitched = new CountDownLatch(1);
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity,
                cameraId,
                new Camera2Capturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {

                    }

                    @Override
                    public void onCameraSwitched(String newCameraId) {
                        assertEquals(expectedCameraId, newCameraId);
                        assertEquals(expectedCameraId,
                                camera2Capturer.getCameraId());
                        cameraSwitched.countDown();
                    }

                    @Override
                    public void onError(Camera2Capturer.Exception exception) {

                    }
                });

        // Switch our camera
        camera2Capturer.switchCamera(expectedCameraId);

        // Now add our video track
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);

        // Validate our switch happened
        assertTrue(cameraSwitched.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));

        // Add renderer
        localVideoTrack.addRenderer(frameCountRenderer);

        // Validate we get a frame
        assertTrue(frameCountRenderer.waitForFrame(CAMERA2_CAPTURER_DELAY_MS));
    }

    @Test
    public void switchCamera_shouldFailWithSwitchPending() throws InterruptedException {
        assumeTrue(cameraIds.length > 1);
        final String cameraId = cameraIds[0];
        final String otherCameraId = cameraIds[1];
        final CountDownLatch cameraSwitchError = new CountDownLatch(1);
        camera2Capturer = new Camera2Capturer(cameraCapturerActivity,
                cameraId,
                new Camera2Capturer.Listener() {
                    @Override
                    public void onFirstFrameAvailable() {}

                    @Override
                    public void onCameraSwitched(String newCameraId) {
                    }

                    @Override
                    public void onError(Camera2Capturer.Exception exception) {
                        assertEquals(Camera2Capturer.Exception.CAMERA_SWITCH_FAILED,
                                exception.getCode());
                        cameraSwitchError.countDown();
                    }
                });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);

        // Switch our cameras quickly
        camera2Capturer.switchCamera(otherCameraId);
        camera2Capturer.switchCamera(otherCameraId);

        // Wait for callback
        assertTrue(cameraSwitchError.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldInvokeListenerCallbacksOnCreationThread() throws InterruptedException {
        assumeTrue(cameraIds.length > 1);
        final String cameraId = cameraIds[0];
        final String otherCameraId = cameraIds[1];
        final CountDownLatch firstFrameAvailable = new CountDownLatch(1);
        final CountDownLatch cameraSwitched = new CountDownLatch(1);
        final CountDownLatch cameraSwitchedFailed = new CountDownLatch(1);
        /*
         * Run on UI thread to avoid thread hopping between the test runner thread and the UI
         * thread.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final long callingThreadId = Thread.currentThread().getId();

                camera2Capturer = new Camera2Capturer(cameraCapturerActivity, cameraId,
                        new Camera2Capturer.Listener() {
                            @Override
                            public void onFirstFrameAvailable() {
                                assertEquals(callingThreadId, Thread.currentThread().getId());
                                firstFrameAvailable.countDown();
                            }

                            @Override
                            public void onCameraSwitched(String cameraId) {
                                assertEquals(callingThreadId, Thread.currentThread().getId());
                                cameraSwitched.countDown();
                            }

                            @Override
                            public void onError(Camera2Capturer.Exception exception) {
                                assertEquals(callingThreadId, Thread.currentThread().getId());
                                cameraSwitchedFailed.countDown();
                            }
                        });
                localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true,
                        camera2Capturer);
            }
        });

        assertTrue(firstFrameAvailable.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
        camera2Capturer.switchCamera(otherCameraId);
        assertTrue(cameraSwitched.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
        camera2Capturer.switchCamera(cameraId);
        camera2Capturer.switchCamera(cameraId);
        assertTrue(cameraSwitchedFailed.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }
}
