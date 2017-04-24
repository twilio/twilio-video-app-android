package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseLocalVideoTrackTest;
import com.twilio.video.util.FakeVideoRenderer;
import com.twilio.video.util.FrameCountRenderer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalVideoTrackTest extends BaseLocalVideoTrackTest {
    private static final int LOCAL_VIDEO_TRACK_TEST_DELAY_MS = 3000;

    private FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() {
        super.setup();
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canCreateEnabledVideoTrack() {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertTrue(localVideoTrack.isEnabled());
        assertTrue(fakeVideoCapturer.isStarted());
    }

    @Test
    public void canCreateDisabledVideoTrack() {
        localVideoTrack = LocalVideoTrack.create(context, false, fakeVideoCapturer);

        assertNotNull(localVideoTrack);
        assertFalse(localVideoTrack.isEnabled());
        assertTrue(fakeVideoCapturer.isStarted());
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

    @Test
    public void addRenderer_shouldSilentlyFailAfterReleased() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(context, true, fakeVideoCapturer);
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.release();
        localVideoTrack.addRenderer(new FakeVideoRenderer());
    }

    @Test
    public void removeRenderer_shouldSilentlyFailAfterReleased() throws InterruptedException {
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
        final int maxRetries = 3;
        int retries = 0;
        while (frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS)) {
            if (retries == maxRetries) {
                fail("Still receiving frames after renderer removed");
            }
            retries++;
        }
    }
}
