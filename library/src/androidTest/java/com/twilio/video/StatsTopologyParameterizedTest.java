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

import com.twilio.video.base.BaseStatsTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class StatsTopologyParameterizedTest extends BaseStatsTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    public StatsTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.baseSetup(topology);
    }

    @After
    @Override
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldReceiveStatsForParticipantTracks() throws InterruptedException {
        // Connect Alice to room with local audio track only
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with audio and video track
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity,
                true,
                new FakeVideoCapturer());
        bobRoom = createRoom(bobToken,
                bobListener,
                roomName,
                Collections.singletonList(bobLocalAudioTrack),
                Collections.singletonList(bobLocalVideoTrack));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);

        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

        List<StatsReport> statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        StatsReport statsReport = statsReportList.get(0);
        expectStatsReportTracksSize(statsReport, 1, 0, 1, 1);
    }

    @Test
    public void shouldReceiveStatsWhenParticipanAddsOrRemovesTrack() throws InterruptedException {
        // Connect Alice to room with local audio track only
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob without media
        bobRoom = createRoom(bobToken, bobListener, roomName);
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());

        // Add audio track to Bob and check stats
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participantListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        Participant bob = aliceRoom.getParticipants().get(0);
        bob.setListener(participantListener);

        LocalParticipant bobLocalParticipant = bobRoom.getLocalParticipant();
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(bobLocalParticipant.addAudioTrack(bobLocalAudioTrack));
        assertTrue(participantListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);
        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

        List<StatsReport> statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        StatsReport statsReport = statsReportList.get(0);
        expectStatsReportTracksSize(statsReport, 1, 0, 1, 0);

        // Add video track to bob and check stats
        bobLocalVideoTrack = LocalVideoTrack
                .create(mediaTestActivity, true, new FakeVideoCapturer());
        assertTrue(bobLocalParticipant.addVideoTrack(bobLocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);
        aliceStatsListener = new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

        statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        statsReport = statsReportList.get(0);
        expectStatsReportTracksSize(statsReport, 1, 0, 1, 1);

        // Remove Bob's video track and check the stats
        bobLocalParticipant.removeVideoTrack(bobLocalVideoTrack);
        assertTrue(participantListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);
        aliceStatsListener = new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

        statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        statsReport = statsReportList.get(0);
        expectStatsReportTracksSize(statsReport, 1, 0, 1, 0);
    }

    @Test
    public void shouldReceiveStatsWhenLocalTrackIsAdded() throws InterruptedException {
        // Connect Alice to room without media
        aliceRoom = createRoom(aliceToken, aliceListener, roomName);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob without tracks
        bobRoom = createRoom(bobToken, bobListener, roomName);
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());


        // Get alice local participant
        LocalParticipant aliceLocalParticipant = aliceRoom.getLocalParticipant();

        // Add audio and video track to alice
        aliceLocalVideoTrack = LocalVideoTrack
                .create(mediaTestActivity, true, new FakeVideoCapturer());
        assertTrue(aliceLocalParticipant.addVideoTrack(aliceLocalVideoTrack));

        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(aliceLocalParticipant.addAudioTrack(aliceLocalAudioTrack));

        // let's give peer connection some time to get media flowing
        Thread.sleep(2500);

        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

        List<StatsReport> statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        StatsReport statsReport = statsReportList.get(0);

        expectStatsReportTracksSize(statsReport, 1, 1, 0, 0);
    }

    @Test
    public void shouldGetStatsForMultipleRequests() throws InterruptedException {
        final int numberOfRequests = 4;

        // Connect Alice to room with local audio track only
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with audio and video track
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobRoom = createRoom(bobToken,
                bobListener,
                roomName,
                Collections.singletonList(bobLocalAudioTrack));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);

        // send getStats() requests
        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(numberOfRequests);
        for (int i = 0; i < numberOfRequests; i++) {
            aliceRoom.getStats(aliceStatsListener);
        }

        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        // check last stats report
        List<StatsReport> statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        expectStatsReportTracksSize(statsReportList.get(0), 1, 0, 1, 0);
    }

    @Test
    @Ignore
    public void reportShouldHaveNonEmptyValues() throws InterruptedException {
        // Connect Alice to room with both video and audio track
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceLocalVideoTrack = LocalVideoTrack.
                create(mediaTestActivity, true, new FakeVideoCapturer());
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack),
                Collections.singletonList(aliceLocalVideoTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with both video and audio track
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobLocalVideoTrack = LocalVideoTrack.
                create(mediaTestActivity, true, new FakeVideoCapturer());
        bobRoom = createRoom(bobToken,
                bobListener,
                roomName,
                Collections.singletonList(bobLocalAudioTrack),
                Collections.singletonList(bobLocalVideoTrack));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        // let's give peer connection some time to get media flowing
        // 10 seconds seems enough to create Jitter. It fails every time with 5s.
        Thread.sleep(10000);

        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        List<StatsReport> statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(1, statsReportList.size());
        StatsReport statsReport = statsReportList.get(0);
        expectStatsReportTracksSize(statsReport, 1, 1, 1, 1);

        assertNotEquals("", statsReport.getPeerConnectionId());

        // Check LocalAudioTrackStats
        LocalAudioTrackStats localAudioTrackStats = statsReport.getLocalAudioTrackStats().get(0);
        checkBaseTrackStats(localAudioTrackStats);
        checkLocalTrackStats(localAudioTrackStats);
        assertTrue(0 < localAudioTrackStats.audioLevel);
        assertTrue(0 < localAudioTrackStats.jitter);
        assertTrue(0 < localAudioTrackStats.roundTripTime);

        // Check LocalVideoTrackStats
        LocalVideoTrackStats localVideoTrackStats = statsReport.getLocalVideoTrackStats().get(0);
        checkBaseTrackStats(localVideoTrackStats);
        checkLocalTrackStats(localVideoTrackStats);
        assertTrue(0 < localVideoTrackStats.capturedFrameRate);
        assertTrue(0 < localVideoTrackStats.captureDimensions.width);
        assertTrue(0 < localVideoTrackStats.captureDimensions.height);
        assertTrue(0 < localVideoTrackStats.frameRate);
        assertTrue(0 < localVideoTrackStats.dimensions.width);
        assertTrue(0 < localVideoTrackStats.dimensions.height);
        assertTrue(0 < localVideoTrackStats.roundTripTime);

        // Check AudioTrackStats
        AudioTrackStats audioTrackStats = statsReport.getAudioTrackStats().get(0);
        checkBaseTrackStats(audioTrackStats);
        checkTrackStats(audioTrackStats);
        assertTrue(0 < audioTrackStats.audioLevel);
        assertTrue(0 < audioTrackStats.jitter);

        // Check VideoTrackStats
        VideoTrackStats videoTrackStats = statsReport.getVideoTrackStats().get(0);
        checkBaseTrackStats(videoTrackStats);
        checkTrackStats(videoTrackStats);
        assertTrue(0 < videoTrackStats.frameRate);
        assertTrue(0 < videoTrackStats.dimensions.width);
        assertTrue(0 < videoTrackStats.dimensions.height);
    }

    @Test
    @Ignore
    public void shouldReceiveEmptyReportsIfRoomGetsDisconnected() throws InterruptedException {
        final int numberOfRequests = 10;

        // Connect Alice to room with local audio track only
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with audio and video track
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobRoom = createRoom(bobToken,
                bobListener,
                roomName,
                Collections.singletonList(bobLocalAudioTrack));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);

        // send getStats() requests
        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(numberOfRequests);
        for (int i = 0; i < numberOfRequests; i++) {
            aliceRoom.getStats(aliceStatsListener);
        }

        // disconnect from room
        aliceListener.onDisconnectedLatch = new CountDownLatch(1);
        aliceRoom.disconnect();
        assertTrue(aliceListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));

        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        List<StatsReport> statsReportList = aliceStatsListener.getStatsReports();
        assertEquals(0, statsReportList.size());
    }


    @Test
    public void shouldNotReceiveReportAfterRoomIsDisconnected() throws InterruptedException {
        // Connect Alice to room with both video and audio track
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceLocalVideoTrack = LocalVideoTrack.
                create(mediaTestActivity, true, new FakeVideoCapturer());
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack),
                Collections.singletonList(aliceLocalVideoTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with both video and audio track
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobLocalVideoTrack = LocalVideoTrack.
                create(mediaTestActivity, true, new FakeVideoCapturer());
        bobRoom = createRoom(bobToken,
                bobListener,
                roomName,
                Collections.singletonList(bobLocalAudioTrack),
                Collections.singletonList(bobLocalVideoTrack));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);

        // let's give peer connection some time to get media flowing
        Thread.sleep(2000);

        aliceListener.onDisconnectedLatch = new CountDownLatch(1);
        aliceRoom.disconnect();

        // wait for disconnect
        assertTrue(aliceListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));

        // call get stats after room has been disconnected
        aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertFalse(aliceStatsListener.onStatsLatch.await(5, TimeUnit.SECONDS));
    }

    private void expectStatsReportTracksSize(StatsReport statsReport, int localAudioTrackSize,
                                             int localVideoTrackSize, int audioTrackSize,
                                             int videoTrackSize) {

        assertEquals(localAudioTrackSize, statsReport.getLocalAudioTrackStats().size());
        assertEquals(localVideoTrackSize, statsReport.getLocalVideoTrackStats().size());
        assertEquals(audioTrackSize, statsReport.getAudioTrackStats().size());
        assertEquals(videoTrackSize, statsReport.getVideoTrackStats().size());
    }

    private void checkBaseTrackStats(BaseTrackStats stats) {
        assertNotEquals("", stats.codec);
        assertNotEquals("", stats.ssrc);
        assertNotEquals("", stats.trackId);
        // TODO: Packets lost is always 0. Find a way to make test that will exercise this
        assertTrue(0 <= stats.packetsLost);
        assertTrue(0.0 < stats.timestamp);
    }

    private void checkLocalTrackStats(LocalTrackStats stats) {
        assertTrue(0 < stats.bytesSent);
        assertTrue(0 < stats.packetsSent);
        assertTrue(0 < stats.roundTripTime);
    }

    private void checkTrackStats(TrackStats stats) {
        assertTrue(0 < stats.bytesReceived);
        assertTrue(0 < stats.packetsReceived);
    }
}
