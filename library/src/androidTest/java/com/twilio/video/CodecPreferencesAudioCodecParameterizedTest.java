/*
 * Copyright (C) 2017 Twilio, inc.
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

import com.twilio.video.base.BaseStatsTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class CodecPreferencesAudioCodecParameterizedTest extends BaseStatsTest {
    @Parameterized.Parameters(name = "Topology: {0}, AudioCodec: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P, AudioCodec.ISAC},
                {Topology.P2P, AudioCodec.OPUS},
                {Topology.P2P, AudioCodec.PCMA},
                {Topology.P2P, AudioCodec.PCMU},
                {Topology.P2P, AudioCodec.G722}

                // TODO: Enable codec preferences tests for group rooms GSDK-1291
                // {Topology.GROUP, AudioCodec.ISAC},
                // {Topology.GROUP, AudioCodec.OPUS},
                // {Topology.GROUP, AudioCodec.PCMA},
                // {Topology.GROUP, AudioCodec.PCMU},
                // {Topology.GROUP, AudioCodec.G722}
        });
    }

    private final Topology topology;
    private final AudioCodec audioCodec;

    public CodecPreferencesAudioCodecParameterizedTest(Topology topology, AudioCodec audioCodec) {
        this.topology = topology;
        this.audioCodec = audioCodec;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        baseSetup(topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
    }

    @Test
    public void canPreferAudioCodec()
            throws InterruptedException {
        // Connect alice with audio track and preferred codec
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .audioTracks(Collections.singletonList(aliceLocalAudioTrack))
                .preferAudioCodecs(Collections.singletonList(audioCodec))
                .build();
        aliceRoom = createRoom(aliceListener, aliceConnectOptions);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect bob with no tracks
        ConnectOptions bobConnectOptions = new ConnectOptions.Builder(bobToken)
                .roomName(roomName)
                .build();
        bobRoom = createRoom(bobListener, bobConnectOptions);
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getRemoteParticipants().size());

        // Give peer connection some time to get media flowing
        Thread.sleep(2000);

        // Get stats for alice and bob
        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        CallbackHelper.FakeStatsListener bobStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        bobStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        bobRoom.getStats(bobStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

        StatsReport aliceStatsReport = aliceStatsListener.getStatsReports().get(0);
        StatsReport bobStatsReport = bobStatsListener.getStatsReports().get(0);

        // Validate that both stats report see the correct codec
        assertEquals(audioCodec.name().toLowerCase(),
                aliceStatsReport.getLocalAudioTrackStats().get(0).codec.toLowerCase());
        assertEquals(audioCodec.name().toLowerCase(),
                bobStatsReport.getRemoteAudioTrackStats().get(0).codec.toLowerCase());
    }
}
