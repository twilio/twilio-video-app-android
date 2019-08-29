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

import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import com.twilio.video.base.BaseCamera2CapturerTest;
import com.twilio.video.testcategories.CapturerTest;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@CapturerTest
@RunWith(Parameterized.class)
public class Camera2CapturerIdParameterizedTest extends BaseCamera2CapturerTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{"0"}, {"1"}});
    }

    private final String cameraId;

    public Camera2CapturerIdParameterizedTest(String cameraId) {
        this.cameraId = cameraId;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
    }

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldCaptureFramesWhenVideoTrackCreated() throws InterruptedException {
        final CountDownLatch firstFrameReceived = new CountDownLatch(1);
        camera2Capturer =
                new Camera2Capturer(
                        cameraCapturerActivity,
                        cameraId,
                        new Camera2Capturer.Listener() {
                            @Override
                            public void onFirstFrameAvailable() {
                                firstFrameReceived.countDown();
                            }

                            @Override
                            public void onCameraSwitched(@NonNull String cameraId) {}

                            @Override
                            public void onError(@NonNull Camera2Capturer.Exception exception) {}
                        });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);

        // Validate we got our first frame
        assertTrue(firstFrameReceived.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void canBeRenderedToView() throws InterruptedException {
        VideoView localVideo =
                (VideoView)
                        cameraCapturerActivity.findViewById(com.twilio.video.test.R.id.local_video);
        final CountDownLatch renderedFirstFrame = new CountDownLatch(1);
        VideoRenderer.Listener rendererListener =
                new VideoRenderer.Listener() {
                    @Override
                    public void onFirstFrame() {
                        renderedFirstFrame.countDown();
                    }

                    @Override
                    public void onFrameDimensionsChanged(int width, int height, int rotation) {}
                };
        localVideo.setListener(rendererListener);
        camera2Capturer =
                new Camera2Capturer(
                        cameraCapturerActivity,
                        cameraId,
                        new Camera2Capturer.Listener() {
                            @Override
                            public void onFirstFrameAvailable() {}

                            @Override
                            public void onCameraSwitched(@NonNull String cameraId) {}

                            @Override
                            public void onError(@NonNull Camera2Capturer.Exception exception) {}
                        });
        localVideoTrack = LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);
        localVideoTrack.addRenderer(localVideo);
        assertTrue(renderedFirstFrame.await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));
        localVideoTrack.removeRenderer(localVideo);
    }

    @Test
    public void canBeReused() throws InterruptedException {
        int reuseCount = 2;

        // Reuse the same capturer while we iterate
        final AtomicReference<CountDownLatch> firstFrameReceived = new AtomicReference<>();
        camera2Capturer =
                new Camera2Capturer(
                        cameraCapturerActivity,
                        cameraId,
                        new Camera2Capturer.Listener() {
                            @Override
                            public void onFirstFrameAvailable() {
                                firstFrameReceived.get().countDown();
                            }

                            @Override
                            public void onCameraSwitched(@NonNull String cameraId) {}

                            @Override
                            public void onError(@NonNull Camera2Capturer.Exception exception) {}
                        });
        for (int i = 0; i < reuseCount; i++) {
            firstFrameReceived.set(new CountDownLatch(1));
            LocalVideoTrack localVideoTrack =
                    LocalVideoTrack.create(cameraCapturerActivity, true, camera2Capturer);

            // Validate we got our first frame
            assertTrue(
                    firstFrameReceived
                            .get()
                            .await(CAMERA2_CAPTURER_DELAY_MS, TimeUnit.MILLISECONDS));

            // Release video track
            localVideoTrack.release();
        }
    }
}
