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
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseCodecTest;
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
public class CodecPreferencesTest extends BaseCodecTest {
    // Audio codecs
    private final IsacCodec isacCodec = new IsacCodec();
    private final OpusCodec opusCodec = new OpusCodec();
    private final PcmaCodec pcmaCodec = new PcmaCodec();
    private final PcmuCodec pcmuCodec = new PcmuCodec();
    private final G722Codec g722Codec = new G722Codec();

    // Video codecs
    private final Vp8Codec vp8Codec = new Vp8Codec();
    private final H264Codec h264Codec = new H264Codec();
    private final Vp9Codec vp9Codec = new Vp9Codec();

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
    }

    @Test
    @Parameters
    public void canPreferAudioCodec(Topology topology, AudioCodec expectedAudioCodec)
            throws InterruptedException {
        baseSetup(topology);

        // Connect alice with audio track and preferred codec
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .audioTracks(Collections.singletonList(aliceLocalAudioTrack))
                .preferAudioCodecs(Collections.singletonList(expectedAudioCodec))
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

        // Validate the codec published
        assertAudioCodecPublished(expectedAudioCodec);
    }

    @Test
    @Parameters
    public void canPreferVideoCodec(Topology topology, VideoCodec expectedVideoCodec)
            throws InterruptedException {
        super.baseSetup(topology);
        if (expectedVideoCodec instanceof H264Codec) {
            assumeTrue(MediaCodecVideoEncoder.isH264HwSupported());
            assumeTrue(MediaCodecVideoDecoder.isH264HwSupported());
        }
        // Connect alice with video track and preferred codec
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .videoTracks(Collections.singletonList(aliceLocalVideoTrack))
                .preferVideoCodecs(Collections.singletonList(expectedVideoCodec))
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

        // Validate published codec
        assertVideoCodecPublished(expectedVideoCodec);
    }

    @Test
    public void canPreferVideoCodecs() throws InterruptedException {
        super.baseSetup(Topology.P2P);

        // Device without H264 support required to test fallback preference
        assumeFalse(MediaCodecVideoEncoder.isH264HwSupported());

        VideoCodec expectedVideoCodec = new Vp9Codec();

        // Connect alice with video track and preferred codecs
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .videoTracks(Collections.singletonList(aliceLocalVideoTrack))
                .preferVideoCodecs(Arrays.asList(new H264Codec(), new Vp9Codec()))
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

        // Validate published codec
        assertVideoCodecPublished(expectedVideoCodec);
    }

    /*
     * Used to supply parameters to test
     */
    @SuppressWarnings("unused")
    private Object[] parametersForCanPreferAudioCodec() {
        return new Object[]{
                new Object[]{Topology.P2P, isacCodec},
                new Object[]{Topology.P2P, opusCodec},
                new Object[]{Topology.P2P, pcmaCodec},
                new Object[]{Topology.P2P, pcmuCodec},
                new Object[]{Topology.P2P, g722Codec}

                // TODO: Enable codec preferences tests for group rooms GSDK-1291
                // new Object[]{Topology.GROUP, isacCodec},
                // new Object[]{Topology.GROUP, opusCodec},
                // new Object[]{Topology.GROUP, pcmaCodec},
                // new Object[]{Topology.GROUP, pcmuCodec},
                // new Object[]{Topology.GROUP, g722Codec}
        };
    }

    /*
     * Used to supply parameters to test
     */
    @SuppressWarnings("unused")
    private Object[] parametersForCanPreferVideoCodec() {
        return new Object[]{
                new Object[]{Topology.P2P, vp8Codec},
                new Object[]{Topology.P2P, h264Codec},
                new Object[]{Topology.P2P, vp9Codec}

                // TODO: Enable codec preferences tests for group rooms GSDK-1291
                // new Object[]{Topology.GROUP, vp8Codec},
                // new Object[]{Topology.GROUP, h264Codec},
                // new Object[]{Topology.GROUP, vp9Codec}
        };
    }
}
