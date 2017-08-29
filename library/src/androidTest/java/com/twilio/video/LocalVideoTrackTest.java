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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;

import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.FakeVideoRenderer;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

import static com.twilio.video.util.VideoAssert.assertNoFramesRendered;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
@SmallTest
public class LocalVideoTrackTest {
    private static final int LOCAL_VIDEO_TRACK_TEST_DELAY_MS = 3000;

    private Context context;
    private LocalVideoTrack localVideoTrack;
    private FakeVideoCapturer fakeVideoCapturer;
    private FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() {
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
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void canCreateVideoTrack(boolean enabled) {
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertEquals(enabled, localVideoTrack.isEnabled());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    @Parameters
    @TestCaseName("{method}[name: {0}]")
    public void canCreateVideoTrackWithName(String name, String expectedName) {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer, name);

        assertNotNull(localVideoTrack);
        assertEquals(expectedName, localVideoTrack.getName());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void isEnabled_shouldReturnFalseAfterReleased(boolean enabled) {
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);
        assertEquals(enabled, localVideoTrack.isEnabled());
        localVideoTrack.release();
        assertFalse(localVideoTrack.isEnabled());
    }

    @Test
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void enable_shouldChangeState(boolean enabled) {
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);
        boolean updatedEnabled = !enabled;

        localVideoTrack.enable(updatedEnabled);

        assertEquals(updatedEnabled, localVideoTrack.isEnabled());
    }

    @Test
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void enable_shouldAllowToggling(boolean enabled) {
        int numIterations = 10;
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

        for (int i = 0 ; i < numIterations ; i++) {
            boolean updatedEnabled = !enabled;

            localVideoTrack.enable(updatedEnabled);

            assertEquals(updatedEnabled, localVideoTrack.isEnabled());
            enabled = updatedEnabled;
        }
    }

    @Test
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void enable_shouldAllowSameState(boolean enabled) {
        int numIterations = 10;
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

        for (int i = 0 ; i < numIterations ; i++) {
            localVideoTrack.enable(enabled);

            assertEquals(enabled, localVideoTrack.isEnabled());
        }
    }

    @Test
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void enable_shouldNotBeAllowedAfterReleased(boolean enabled) {
        boolean updatedEnabled = !enabled;
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);

        localVideoTrack.release();
        localVideoTrack.enable(updatedEnabled);

        assertEquals(false, localVideoTrack.isEnabled());
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
        boolean[] expectedEnabled = new boolean[]{ false, true, true, false, false };

        for (int i = 0 ; i < numVideoTracks ; i++) {
            LocalVideoTrack localVideoTrack = LocalVideoTrack.create(context, expectedEnabled[i],
                    fakeVideoCapturer);

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
        VideoConstraints validVideoConstraints = new VideoConstraints.Builder()
                .minVideoDimensions(new VideoDimensions(expectedMinWidth, expectedMinHeight))
                .maxVideoDimensions(new VideoDimensions(expectedMaxWidth, expectedMaxHeight))
                .minFps(expectedMinFps)
                .maxFps(expectedMaxFps)
                .build();
        localVideoTrack = LocalVideoTrack.create(context,
                true, fakeVideoCapturer, validVideoConstraints);

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

    private Object[] parametersForCanCreateVideoTrackWithName() {
        String videoTrackName = RandUtils.generateRandomString(10);

        return new Object[]{
                new Object[]{null, ""},
                new Object[]{"", ""},
                new Object[]{videoTrackName, videoTrackName}
        };
    }
}
