/*
 * Copyright (C) 2018 Twilio, Inc.
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
import android.util.Log;

import com.kevinmost.junit_retry_rule.Retry;
import com.kevinmost.junit_retry_rule.RetryRule;
import com.twilio.video.base.BaseCodecTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class MultiCodecParameterizedTest extends BaseCodecTest {
    private static final Vp8Codec vp8Codec = new Vp8Codec();
    private static final Vp8Codec vp8WithSimulcastCodec = new Vp8Codec(true);
    private static final H264Codec h264Codec = new H264Codec();

    @Parameterized.Parameters(name = "preferred: {0} selected: {1} expected: {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Collections.singletonList(vp8Codec),
                        Collections.singletonList(vp8Codec),
                        vp8Codec},
                {Collections.singletonList(vp8WithSimulcastCodec),
                        Collections.singletonList(vp8WithSimulcastCodec),
                        vp8WithSimulcastCodec},
                {Collections.singletonList(vp8Codec),
                        Arrays.asList(vp8Codec, h264Codec),
                        vp8Codec},
                {Collections.singletonList(vp8WithSimulcastCodec),
                        Arrays.asList(vp8WithSimulcastCodec, h264Codec),
                        vp8WithSimulcastCodec},
                {Arrays.asList(vp8Codec, h264Codec),
                        Collections.singletonList(vp8Codec),
                        vp8Codec},
                {Arrays.asList(vp8WithSimulcastCodec, h264Codec),
                        Collections.singletonList(vp8WithSimulcastCodec),
                        vp8WithSimulcastCodec},
                {Arrays.asList(vp8Codec, h264Codec),
                        Collections.singletonList(h264Codec),
                        h264Codec},
                {Arrays.asList(vp8WithSimulcastCodec, h264Codec),
                        Collections.singletonList(h264Codec),
                        h264Codec},
                {Arrays.asList(vp8Codec, h264Codec),
                        Arrays.asList(vp8Codec, h264Codec),
                        vp8Codec},
                {Arrays.asList(vp8WithSimulcastCodec, h264Codec),
                        Arrays.asList(vp8WithSimulcastCodec, h264Codec),
                        vp8WithSimulcastCodec},
                {Arrays.asList(h264Codec, vp8Codec),
                        Collections.singletonList(vp8Codec),
                        vp8Codec},
                {Arrays.asList(h264Codec, vp8WithSimulcastCodec),
                        Collections.singletonList(vp8WithSimulcastCodec),
                        vp8WithSimulcastCodec},
                {Arrays.asList(h264Codec, vp8Codec),
                        Collections.singletonList(h264Codec),
                        h264Codec},
                {Arrays.asList(h264Codec, vp8WithSimulcastCodec),
                        Collections.singletonList(h264Codec),
                        h264Codec},
                {Arrays.asList(h264Codec, vp8Codec),
                        Arrays.asList(vp8Codec, h264Codec),
                        h264Codec},
                {Arrays.asList(h264Codec, vp8WithSimulcastCodec),
                        Arrays.asList(vp8WithSimulcastCodec, h264Codec),
                        h264Codec}});
    }

    @Rule
    public final RetryRule retryRule = new RetryRule();

    private final List<VideoCodec> preferredVideoCodecs;
    private final List<VideoCodec> selectedVideoCodecs;
    private final VideoCodec expectedCodec;

    public MultiCodecParameterizedTest(List<VideoCodec> preferredVideoCodecs,
                                       List<VideoCodec> selectedVideoCodecs,
                                       VideoCodec expectedCodec) {
        this.preferredVideoCodecs = preferredVideoCodecs;
        this.selectedVideoCodecs = selectedVideoCodecs;
        this.expectedCodec = expectedCodec;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void shouldPublishVideoTrackWhenCodecSupported() throws InterruptedException {
        // Validate device supported H264
        if (expectedCodec instanceof H264Codec &&
                (!MediaCodecVideoEncoder.isH264HwSupported() ||
                        !MediaCodecVideoDecoder.isH264HwSupported())) {
            Log.i("MultiCodecParameterizedTest", "Skipping test because H.264 is " +
                    "required");
            return;
        }

        baseSetup(Topology.GROUP, selectedVideoCodecs);

        // Connect alice with video track and preferred codecs
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .videoTracks(Collections.singletonList(aliceLocalVideoTrack))
                .preferVideoCodecs(preferredVideoCodecs)
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
        assertVideoCodecPublished(expectedCodec);
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void publishTrack_shouldAllowSupportedVideoCodec() throws InterruptedException {
        // Validate device supported H264
        if (expectedCodec instanceof H264Codec &&
                (!MediaCodecVideoEncoder.isH264HwSupported() ||
                        !MediaCodecVideoDecoder.isH264HwSupported())) {
            Log.i("MultiCodecParameterizedTest", "Skipping test because H.264 is " +
                    "required");
            return;
        }

        baseSetup(Topology.GROUP, selectedVideoCodecs);

        // Connect alice with preferred codecs
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .preferVideoCodecs(preferredVideoCodecs)
                .build();
        aliceRoom = createRoom(aliceListener, aliceConnectOptions);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect bob with no tracks
        ConnectOptions bobConnectOptions = new ConnectOptions.Builder(bobToken)
                .roomName(roomName)
                .build();
        bobRoom = createRoom(bobListener, bobConnectOptions);
        RemoteParticipant aliceRemoteParticipant = bobRoom.getRemoteParticipants().get(0);
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getRemoteParticipants().size());

        // Alice publish video track
        LocalParticipant aliceLocalParticipant = aliceRoom.getLocalParticipant();
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        aliceRemoteParticipant.setListener(participantListener);
        aliceLocalParticipant.setListener(localParticipantListener);
        aliceLocalParticipant.publishTrack(aliceLocalVideoTrack);
        assertTrue(localParticipantListener.onPublishedVideoTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));

        // Validate the codec published
        assertVideoCodecPublished(expectedCodec);
    }
}
