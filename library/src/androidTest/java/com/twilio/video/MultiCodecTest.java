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

import com.twilio.video.base.BaseCodecTest;
import com.twilio.video.base.BaseStatsTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.StringUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
public class MultiCodecTest extends BaseCodecTest {

    @Before
    public void setup() throws InterruptedException {
        super.setup();
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
    }

    @Test
    public void publishTrack_shouldFailWithUnsupportedVideoCodec() throws InterruptedException {
        // Select H264 for room
        baseSetup(Topology.GROUP, Collections.singletonList(VideoCodec.H264));

        // Ensure that device does not support H264
        assumeFalse(MediaCodecVideoEncoder.isH264HwSupported());
        assumeFalse(MediaCodecVideoDecoder.isH264HwSupported());

        // Connect alice with VP8 preferred
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(roomName)
                .preferVideoCodecs(Collections.singletonList(VideoCodec.VP8))
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

        // Alice publish video track
        LocalParticipant aliceLocalParticipant = aliceRoom.getLocalParticipant();
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onVideoTrackPublicationFailedLatch = new CountDownLatch(1);
        aliceLocalParticipant.setListener(localParticipantListener);
        aliceLocalParticipant.publishTrack(aliceLocalVideoTrack);

        // Validate the track publication failed
        assertTrue(localParticipantListener.onVideoTrackPublicationFailedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(TwilioException.MEDIA_NO_SUPPORTED_CODEC_EXCEPTION,
                localParticipantListener.publicationFailures.get(aliceLocalVideoTrack).getCode());
    }

    @Test
    @Parameters(method = "supportedVideoCodecs")
    public void shouldPublishVideoTrackWhenCodecSupported(List<VideoCodec> preferredVideoCodecs,
                                                          List<VideoCodec> selectedVideoCodecs,
                                                          VideoCodec expectedCodec) throws InterruptedException {
        baseSetup(Topology.GROUP, selectedVideoCodecs);

        // Validate device supported H264
        if (expectedCodec == VideoCodec.H264) {
            assumeTrue(MediaCodecVideoEncoder.isH264HwSupported());
            assumeTrue(MediaCodecVideoDecoder.isH264HwSupported());
        }

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
    @Parameters(method = "supportedVideoCodecs")
    public void publishTrack_shouldAllowSupportedVideoCodec(List<VideoCodec> preferredVideoCodecs,
                                                            List<VideoCodec> selectedVideoCodecs,
                                                            VideoCodec expectedCodec) throws InterruptedException {
        baseSetup(Topology.GROUP, selectedVideoCodecs);
        if (expectedCodec == VideoCodec.H264) {
            assumeTrue(MediaCodecVideoEncoder.isH264HwSupported());
            assumeTrue(MediaCodecVideoDecoder.isH264HwSupported());
        }
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

    private Object[] supportedVideoCodecs() {
        return new Object[]{
                new Object[]{Collections.singletonList(VideoCodec.VP8),
                        Collections.singletonList(VideoCodec.VP8),
                        VideoCodec.VP8},
                new Object[]{Collections.singletonList(VideoCodec.VP8),
                        Arrays.asList(VideoCodec.VP8, VideoCodec.H264),
                        VideoCodec.VP8},
                new Object[]{Arrays.asList(VideoCodec.VP8, VideoCodec.H264),
                        Collections.singletonList(VideoCodec.VP8),
                        VideoCodec.VP8},
                new Object[]{Arrays.asList(VideoCodec.VP8, VideoCodec.H264),
                        Collections.singletonList(VideoCodec.H264),
                        VideoCodec.H264},
                new Object[]{Arrays.asList(VideoCodec.VP8, VideoCodec.H264),
                        Arrays.asList(VideoCodec.VP8, VideoCodec.H264),
                        VideoCodec.VP8},
                new Object[]{Arrays.asList(VideoCodec.H264, VideoCodec.VP8),
                        Collections.singletonList(VideoCodec.VP8),
                        VideoCodec.VP8},
                new Object[]{Arrays.asList(VideoCodec.H264, VideoCodec.VP8),
                        Collections.singletonList(VideoCodec.H264),
                        VideoCodec.H264},
                new Object[]{Arrays.asList(VideoCodec.H264, VideoCodec.VP8),
                        Arrays.asList(VideoCodec.VP8, VideoCodec.H264),
                        VideoCodec.H264}
        };
    }
}
