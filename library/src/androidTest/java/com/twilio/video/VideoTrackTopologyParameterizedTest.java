package com.twilio.video;

import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class VideoTrackTopologyParameterizedTest extends BaseParticipantTest {
    private static final int VIDEO_TRACK_TEST_DELAY_MS = 3000;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.SFU}});
    }

    private final Topology topology;

    public VideoTrackTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.baseSetup(topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void addRenderer_shouldNotCrashForNullRenderer() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<VideoTrack> videoTracks = participant.getVideoTracks();
        assertEquals(1, videoTracks.size());
        videoTracks.get(0).addRenderer(null);
    }

    @Test
    public void canAddAndRemoveRenderer() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<VideoTrack> videoTracks = participant.getVideoTracks();
        assertEquals(1, videoTracks.size());
        FrameCountRenderer frameCountRenderer = new FrameCountRenderer();
        videoTracks.get(0).addRenderer(frameCountRenderer);
        assertEquals(1, videoTracks.get(0).getRenderers().size());
        assertTrue(frameCountRenderer.waitForFrame(VIDEO_TRACK_TEST_DELAY_MS));
        videoTracks.get(0).removeRenderer(frameCountRenderer);
        assertEquals(0, videoTracks.get(0).getRenderers().size());
    }

    @Test
    public void shouldFailToAddRendererOnRemovedTrack() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.setListener(participantListener);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        assertTrue(actor2LocalParticipant.addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<VideoTrack> videoTracks = participant.getVideoTracks();
        assertEquals(1, videoTracks.size());
        FrameCountRenderer frameCountRenderer = new FrameCountRenderer();
        VideoTrack videoTrack = videoTracks.get(0);

        actor1RoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        actor2Room.disconnect();
        assertTrue(actor1RoomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));

        videoTrack.addRenderer(frameCountRenderer);
        assertFalse(frameCountRenderer.waitForFrame(VIDEO_TRACK_TEST_DELAY_MS));
    }
}
