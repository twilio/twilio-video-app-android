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
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

@RunWith(JUnitParamsRunner.class)
@LargeTest
public class CodecPreferencesTest extends BaseStatsTest {
    @After
    public void teardown() throws InterruptedException {
        super.teardown();
    }

    @Test
    @Parameters
    public void canPreferAudioCodec(Topology topology, AudioCodec audioCodec)
            throws InterruptedException {
        baseSetup(topology);

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

    @Test
    @Parameters
    public void canPreferVideoCodec(Topology topology, VideoCodec videoCodec)
            throws InterruptedException {
        super.baseSetup(topology);
        if (videoCodec == VideoCodec.H264) {
            assumeTrue(MediaCodecVideoEncoder.isH264HwSupported());
            assumeTrue(MediaCodecVideoDecoder.isH264HwSupported());
        }
        // Connect alice with video track and preferred codec
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .videoTracks(Collections.singletonList(aliceLocalVideoTrack))
                .preferVideoCodecs(Collections.singletonList(videoCodec))
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
        assertEquals(videoCodec.name().toLowerCase(),
                aliceStatsReport.getLocalVideoTrackStats().get(0).codec.toLowerCase());
        assertEquals(videoCodec.name().toLowerCase(),
                bobStatsReport.getRemoteVideoTrackStats().get(0).codec.toLowerCase());
    }

    @Test
    public void canPreferVideoCodecs() throws InterruptedException {
        super.baseSetup(Topology.P2P);

        // Device without H264 support required to test fallback preference
        assumeFalse(MediaCodecVideoEncoder.isH264HwSupported());

        VideoCodec expectedVideoCodec = VideoCodec.VP9;

        // Connect alice with video track and preferred codecs
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .videoTracks(Collections.singletonList(aliceLocalVideoTrack))
                .preferVideoCodecs(Arrays.asList(VideoCodec.H264, VideoCodec.VP9))
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

        // Validate that fallback preference
        assertEquals(expectedVideoCodec.name().toLowerCase(),
                aliceStatsReport.getLocalVideoTrackStats().get(0).codec.toLowerCase());
        assertEquals(expectedVideoCodec.name().toLowerCase(),
                bobStatsReport.getRemoteVideoTrackStats().get(0).codec.toLowerCase());
    }

    private Object[] parametersForCanPreferAudioCodec() {
        return new Object[]{
                new Object[]{Topology.P2P, AudioCodec.ISAC},
                new Object[]{Topology.P2P, AudioCodec.OPUS},
                new Object[]{Topology.P2P, AudioCodec.PCMA},
                new Object[]{Topology.P2P, AudioCodec.PCMU},
                new Object[]{Topology.P2P, AudioCodec.G722}

                // TODO: Enable codec preferences tests for group rooms GSDK-1291
                // new Object[]{Topology.GROUP, AudioCodec.ISAC},
                // new Object[]{Topology.GROUP, AudioCodec.OPUS},
                // new Object[]{Topology.GROUP, AudioCodec.PCMA},
                // new Object[]{Topology.GROUP, AudioCodec.PCMU},
                // new Object[]{Topology.GROUP, AudioCodec.G722}
        };
    }

    private Object[] parametersForCanPreferVideoCodec() {
        return new Object[]{
                new Object[]{Topology.P2P, VideoCodec.VP8},
                new Object[]{Topology.P2P, VideoCodec.H264},
                new Object[]{Topology.P2P, VideoCodec.VP9}

                // TODO: Enable codec preferences tests for group rooms GSDK-1291
                // new Object[]{Topology.GROUP, VideoCodec.VP8},
                // new Object[]{Topology.GROUP, VideoCodec.H264},
                // new Object[]{Topology.GROUP, VideoCodec.VP9}
        };
    }
}
