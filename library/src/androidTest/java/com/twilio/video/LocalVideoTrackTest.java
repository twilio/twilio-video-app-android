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
        localMedia.removeVideoTrack(localVideoTrack);
        super.teardown();
    }

    @Test
    public void canAddRenderer() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        assertTrue(frameCountRenderer.getFrameCount() > 0);
    }

    @Test
    public void canRemoveRenderer() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        int frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount > 0);

        localVideoTrack.removeRenderer(frameCountRenderer);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        boolean framesNotRenderering = frameCount >= (frameCountRenderer.getFrameCount() - 1);
        assertTrue(framesNotRenderering);
    }

    @Test
    public void addRenderer_shouldSilentlyFailAfterRemoved() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        assertTrue(frameCountRenderer.getFrameCount() > 0);

        localMedia.removeVideoTrack(localVideoTrack);
        localVideoTrack.addRenderer(new FakeVideoRenderer());
    }

    @Test
    public void removeRenderer_shouldSilentlyFailAfterRemoved() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        assertTrue(frameCountRenderer.getFrameCount() > 0);

        localVideoTrack.removeRenderer(frameCountRenderer);
    }

    @Test
    public void rendereringShouldStopAfterRemoved() throws InterruptedException {
        localVideoTrack.addRenderer(frameCountRenderer);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        int frameCount = frameCountRenderer.getFrameCount();
        assertTrue(frameCount > 0);

        localMedia.removeVideoTrack(localVideoTrack);
        Thread.sleep(LOCAL_VIDEO_TRACK_TEST_DELAY_MS);

        boolean framesNotRenderering = frameCount >= (frameCountRenderer.getFrameCount() - 1);
        assertTrue(framesNotRenderering);
    }
}
