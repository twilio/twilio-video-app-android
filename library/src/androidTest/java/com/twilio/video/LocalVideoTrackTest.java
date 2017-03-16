package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseLocalVideoTrackTest;
import com.twilio.video.util.FakeVideoRenderer;
import com.twilio.video.util.FrameCountRenderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalVideoTrackTest extends BaseLocalVideoTrackTest {
    private static final int LOCAL_VIDEO_TRACK_TEST_DELAY_MS = 3000;

    private LocalVideoTrack localVideoTrack;
    private FrameCountRenderer frameCountRenderer;

    @Before
    public void setup() {
        super.setup();
        localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);
        frameCountRenderer = new FrameCountRenderer();
    }

    @After
    public void teardown() {
        if (localMedia != null) {
            localMedia.removeVideoTrack(localVideoTrack);
        }
        super.teardown();
    }

    @Test
    public void canAddRenderer() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
    }

    @Test
    public void canRemoveRenderer() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.removeRenderer(frameCountRenderer);
    }

    @Test
    public void addRenderer_shouldSilentlyFailAfterRemoved() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localMedia.removeVideoTrack(localVideoTrack);
        localVideoTrack.addRenderer(new FakeVideoRenderer());
    }

    @Test
    public void removeRenderer_shouldSilentlyFailAfterRemoved() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localVideoTrack.removeRenderer(frameCountRenderer);
    }

    @Test
    public void rendereringShouldStopAfterRemoved() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        assertTrue(frameCountRenderer.waitForFrame(LOCAL_VIDEO_TRACK_TEST_DELAY_MS));
        localMedia.removeVideoTrack(localVideoTrack);

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
