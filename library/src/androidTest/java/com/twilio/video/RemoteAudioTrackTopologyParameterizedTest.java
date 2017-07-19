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

import static com.twilio.video.util.VideoAssert.assertIsTrackSid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@LargeTest
public class RemoteAudioTrackTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    private final Topology topology;

    public RemoteAudioTrackTopologyParameterizedTest(Topology topology) {
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
    public void shouldHaveTrackSidAfterAdded() throws InterruptedException {
        // TODO: GSDK-1249 Re-enable once track sid changes are deployed to prod SFU
        assumeTrue(topology == Topology.P2P);
        publishAudioTrack();

        // Validate track was added
        List<RemoteAudioTrack> remoteAudioTracks = remoteParticipant.getRemoteAudioTracks();
        assertEquals(2, remoteAudioTracks.size());

        // Validate track sid
        assertIsTrackSid(remoteAudioTracks.get(1).getSid());
    }

    private void publishAudioTrack() throws InterruptedException {
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        bobPublishableLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(bobLocalParticipant.publishAudioTrack(bobPublishableLocalAudioTrack));
        assertTrue(participantListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }
}
