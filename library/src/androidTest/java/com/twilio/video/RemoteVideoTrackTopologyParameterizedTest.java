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

import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.FrameCountRenderer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.twilio.video.util.VideoAssert.assertFramesRendered;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.twilio.video.util.VideoAssert.assertIsTrackSid;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@LargeTest
public class RemoteVideoTrackTopologyParameterizedTest extends BaseParticipantTest {
    private static final int VIDEO_TRACK_TEST_DELAY_MS = 10000;

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    private final Topology topology;

    public RemoteVideoTrackTopologyParameterizedTest(Topology topology) {
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
    public void shouldHaveTrackSidAfterPublished() throws InterruptedException {
        // TODO: GSDK-1249 Re-enable once track sid changes are deployed to prod SFU
        assumeTrue(topology == Topology.P2P);
        publishVideoTrack();

        // Validate track was added
        List<RemoteVideoTrack> remoteVideoTracks = remoteParticipant.getRemoteVideoTracks();
        assertEquals(1, remoteVideoTracks.size());

        // Validate track sid
        assertIsTrackSid(remoteVideoTracks.get(0).getSid());
    }

    @Test
    public void canBeRendered() throws InterruptedException {
        publishVideoTrack();
        List<RemoteVideoTrack> remoteVideoTracks = remoteParticipant.getRemoteVideoTracks();
        assertEquals(1, remoteVideoTracks.size());
        FrameCountRenderer frameCountRenderer = new FrameCountRenderer();
        remoteVideoTracks.get(0).addRenderer(frameCountRenderer);
        assertEquals(1, remoteVideoTracks.get(0).getRenderers().size());
        assertFramesRendered(frameCountRenderer, VIDEO_TRACK_TEST_DELAY_MS);
        remoteVideoTracks.get(0).removeRenderer(frameCountRenderer);
        assertEquals(0, remoteVideoTracks.get(0).getRenderers().size());
    }

    @Test
    public void shouldEnableVideoTrackAfterConnectedToRoom() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
            new CallbackHelper.FakeParticipantListener();
        participantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        participantListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        bobLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, false,
                new FakeVideoCapturer());

        assertTrue(bobLocalParticipant.publishVideoTrack(bobLocalVideoTrack));
        assertTrue(participantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));
        assertFalse(aliceRoom.getRemoteParticipants().get(0).getRemoteVideoTracks().get(0)
                .isEnabled());
        bobLocalVideoTrack.enable(true);
        assertTrue(participantListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));
        assertTrue(aliceRoom.getRemoteParticipants().get(0).getRemoteVideoTracks().get(0)
                .isEnabled());
    }

    private void publishVideoTrack() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        bobLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        assertTrue(bobLocalParticipant.publishVideoTrack(bobLocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));
    }
}
