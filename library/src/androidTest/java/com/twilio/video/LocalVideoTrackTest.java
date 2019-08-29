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

import static com.twilio.video.TestUtils.TWO_SECONDS;
import static com.twilio.video.util.VideoAssert.assertNoFramesRendered;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.testcategories.MediaTest;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.FakeVideoRenderer;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediaTest
@RunWith(AndroidJUnit4.class)
@SmallTest
public class LocalVideoTrackTest extends BaseVideoTest {
    private static final int LOCAL_VIDEO_TRACK_TEST_DELAY_MS = 3000;

    private Context context;
    private LocalVideoTrack localVideoTrack;
    private FakeVideoCapturer fakeVideoCapturer;
    private FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        fakeVideoCapturer = new FakeVideoCapturer();
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canCreateDisabledVideoTrack() {
        localVideoTrack = LocalVideoTrack.create(context, false, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertFalse(localVideoTrack.isEnabled());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void canCreateEnabledVideoTrack() {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertTrue(localVideoTrack.isEnabled());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void canCreateVideoTrackWithName() {
        String expectedName = random(10);
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer, expectedName);

        assertNotNull(localVideoTrack);
        assertEquals(expectedName, localVideoTrack.getName());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void canCreateVideoTrackWithNullName() {
        String nullName = null;
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer, nullName);

        assertNotNull(localVideoTrack);
        assertFalse(StringUtils.isNullOrEmpty(localVideoTrack.getName()));
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void canCreateVideoTrackWithEmptyName() {
        String emptyName = "";
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer, emptyName);

        assertNotNull(localVideoTrack);
        assertFalse(StringUtils.isNullOrEmpty(localVideoTrack.getName()));
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void isEnabled_shouldReturnFalseAfterReleased() {
        boolean[] enabledParameters = new boolean[] {false, true};

        for (boolean enabled : enabledParameters) {
            localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);
            assertEquals(enabled, localVideoTrack.isEnabled());
            localVideoTrack.release();
            assertFalse(localVideoTrack.isEnabled());
        }
    }

    @Test
    public void enable_shouldChangeState() {
        boolean[] enabledParameters = new boolean[] {false, true};

        for (boolean enabled : enabledParameters) {
            localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);
            boolean updatedEnabled = !enabled;

            localVideoTrack.enable(updatedEnabled);
            assertEquals(updatedEnabled, localVideoTrack.isEnabled());
            localVideoTrack.release();
        }
    }

    @Test
    public void enable_shouldAllowToggling() {
        boolean[] enabledParameters = new boolean[] {false, true};

        for (boolean enabled : enabledParameters) {
            int numIterations = 10;
            localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

            for (int i = 0; i < numIterations; i++) {
                boolean updatedEnabled = !enabled;

                localVideoTrack.enable(updatedEnabled);

                assertEquals(updatedEnabled, localVideoTrack.isEnabled());
                enabled = updatedEnabled;
            }
            localVideoTrack.release();
        }
    }

    @Test
    public void enable_shouldAllowSameState() {
        boolean[] enabledParameters = new boolean[] {false, true};

        for (boolean enabled : enabledParameters) {
            int numIterations = 10;
            localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

            for (int i = 0; i < numIterations; i++) {
                localVideoTrack.enable(enabled);

                assertEquals(enabled, localVideoTrack.isEnabled());
            }
            localVideoTrack.release();
        }
    }

    @Test
    public void enable_shouldNotBeAllowedAfterReleased() {
        boolean[] enabledParameters = new boolean[] {false, true};

        for (boolean enabled : enabledParameters) {
            boolean updatedEnabled = !enabled;
            localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

            localVideoTrack.release();
            localVideoTrack.enable(updatedEnabled);

            assertEquals(false, localVideoTrack.isEnabled());
        }
    }

    @Test
    public void canReleaseVideoTrack() {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        localVideoTrack.release();
    }

    @Test
    public void release_shouldBeIdempotent() {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        localVideoTrack.release();
        localVideoTrack.release();
    }

    @Test
    public void canCreateMultipleVideoTracks() {
        int numVideoTracks = 5;
        boolean[] expectedEnabled = new boolean[] {false, true, true, false, false};

        for (int i = 0; i < numVideoTracks; i++) {
            LocalVideoTrack localVideoTrack =
                    LocalVideoTrack.create(context, expectedEnabled[i], fakeVideoCapturer);

            assertNotNull(localVideoTrack);
            assertEquals(expectedEnabled[i], localVideoTrack.isEnabled());
            localVideoTrack.release();
        }
    }

    @Test
    public void create_shouldRespectValidConstraints() {
        Integer expectedMinWidth = 320;
        Integer expectedMinHeight = 180;
        Integer expectedMaxWidth = 640;
        Integer expectedMaxHeight = 360;
        Integer expectedMinFps = 5;
        Integer expectedMaxFps = 30;
        VideoConstraints validVideoConstraints =
                new VideoConstraints.Builder()
                        .minVideoDimensions(
                                new VideoDimensions(expectedMinWidth, expectedMinHeight))
                        .maxVideoDimensions(
                                new VideoDimensions(expectedMaxWidth, expectedMaxHeight))
                        .minFps(expectedMinFps)
                        .maxFps(expectedMaxFps)
                        .build();
        localVideoTrack =
                LocalVideoTrack.create(context, true, fakeVideoCapturer, validVideoConstraints);

        Assert.assertNotNull(localVideoTrack);

        VideoFormat captureFormat = fakeVideoCapturer.getCaptureFormat();
        assertTrue(captureFormat.dimensions.width >= expectedMinWidth);
        assertTrue(captureFormat.dimensions.width <= expectedMaxWidth);
        assertTrue(captureFormat.dimensions.height >= expectedMinHeight);
        assertTrue(captureFormat.dimensions.height <= expectedMaxHeight);
        assertTrue(captureFormat.framerate >= expectedMinFps);
        assertTrue(captureFormat.framerate <= expectedMaxFps);
    }

    @Test
    public void canAddRenderer() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);
        localVideoTrack.addRenderer(frameCountRenderer);

        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
    }

    @Test
    public void canRemoveRenderer() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.removeRenderer(frameCountRenderer);
    }

    @Test(expected = IllegalStateException.class)
    public void addRenderer_shouldFailAfterReleased() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.release();
        localVideoTrack.addRenderer(new FakeVideoRenderer());
    }

    @Test(expected = IllegalStateException.class)
    public void removeRenderer_shouldFailAfterReleased() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.release();
        localVideoTrack.removeRenderer(frameCountRenderer);
    }

    @Test
    public void rendereringShouldStopAfterReleased() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.release();

        // Validate that eventually frame events stop
        assertNoFramesRendered(frameCountRenderer, LOCAL_VIDEO_TRACK_TEST_DELAY_MS);
    }

    @Test
    @TargetApi(28)
    public void CanSupportMultipleCameraStreams() throws InterruptedException {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);
        final int cameraCount = 2;
        final CountDownLatch cameraFrameLatch = new CountDownLatch(cameraCount);

        final List<Camera2Capturer> capturers = new ArrayList<>();
        List<LocalVideoTrack> localVideoTrackList = new ArrayList<>();
        for (int i = 0; i < cameraCount; i++) {
            final Camera2Capturer camera =
                    new Camera2Capturer(
                            context,
                            String.valueOf(i),
                            new Camera2Capturer.Listener() {
                                @Override
                                public void onFirstFrameAvailable() {
                                    cameraFrameLatch.countDown();
                                }

                                @Override
                                public void onCameraSwitched(@NonNull String newCameraId) {}

                                @Override
                                public void onError(
                                        @NonNull
                                                Camera2Capturer.Exception
                                                        camera2CapturerException) {}
                            });
            capturers.add(camera);
            localVideoTrack = LocalVideoTrack.create(context, true, camera);
            localVideoTrack.addRenderer(frameCountRenderer);
            localVideoTrackList.add(localVideoTrack);
        }
        assertTrue(cameraFrameLatch.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        for (VideoTrack videoTrack : localVideoTrackList) {
            videoTrack.release();
        }
        localVideoTrackList.clear();

        for (Camera2Capturer capturer : capturers) {
            capturer.stopCapture();
        }
        capturers.clear();

        InstrumentationRegistry.getInstrumentation()
                .runOnMainSync(
                        () -> {
                            try {
                                TestUtils.blockingWait(TWO_SECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
    }
}
