package com.twilio.video;


import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class StatsTopologyParameterizedTest extends BaseClientTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String aliceToken, bobToken;
    private String roomName;
    private Room aliceRoom, bobRoom;
    private LocalVideoTrack aliceLocalVideoTrack;
    private LocalAudioTrack aliceLocalAudioTrack;
    private LocalVideoTrack bobLocalVideoTrack;
    private LocalAudioTrack bobLocalAudioTrack;
    private CallbackHelper.FakeRoomListener aliceListener, bobListener;
    private CallbackHelper.FakeParticipantListener aliceMediaListener, bobMediaListener;
    private final Topology topology;

    public StatsTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        roomName = RandUtils.generateRandomString(20);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
        aliceListener = new CallbackHelper.FakeRoomListener();
        aliceMediaListener = new CallbackHelper.FakeParticipantListener();
        bobMediaListener = new CallbackHelper.FakeParticipantListener();
        bobListener = new CallbackHelper.FakeRoomListener();
    }

    @After
    public void teardown() throws InterruptedException{
        roomTearDown(aliceRoom);
        roomTearDown(bobRoom);
        if (aliceLocalAudioTrack != null) {
            aliceLocalAudioTrack.release();
        }
        if (aliceLocalVideoTrack != null) {
            aliceLocalVideoTrack.release();
        }
        if (bobLocalAudioTrack != null) {
            bobLocalAudioTrack.release();
        }
        if (bobLocalVideoTrack != null) {
            bobLocalVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullListener() throws InterruptedException {
        aliceRoom = createRoom(aliceToken, aliceListener, roomName);
        aliceRoom.getStats(null);
    }

    @Test
    public void shouldReceiveStatsInEmptyRoom() throws InterruptedException {
        aliceRoom = createRoom(aliceToken, aliceListener, roomName);

        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        assertTrue(1 >= aliceStatsListener.getStatsReports().size());
    }

    @Test
    public void shouldInvokeListenerOnCallingThread() throws InterruptedException {
        // Connect Alice to room with local audio track only
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);
        final CountDownLatch statsCallback = new CountDownLatch(1);

        /*
         * Run on UI thread to avoid thread hopping between the test runner thread and the UI
         * thread.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final long callingThreadId = Thread.currentThread().getId();
                StatsListener statsListener = new StatsListener() {
                    @Override
                    public void onStats(List<StatsReport> statsReports) {
                        assertEquals(callingThreadId, Thread.currentThread().getId());
                        statsCallback.countDown();
                    }
                };
                aliceRoom.getStats(statsListener);
            }
        });

        assertTrue(statsCallback.await(20, TimeUnit.SECONDS));
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
        assertEquals(1, aliceRoom.getRemoteParticipants().size());

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
        assertEquals(1, aliceRoom.getRemoteParticipants().size());

        // Add audio track to Bob and check stats
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participantListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        RemoteParticipant bob = aliceRoom.getRemoteParticipants().get(0);
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
        assertEquals(1, aliceRoom.getRemoteParticipants().size());


        // Get alice local remoteParticipant
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
        assertEquals(1, aliceRoom.getRemoteParticipants().size());

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

        // Check RemoteAudioTrackStats
        RemoteAudioTrackStats remoteAudioTrackStats = statsReport.getRemoteAudioTrackStats().get(0);
        checkBaseTrackStats(remoteAudioTrackStats);
        checkTrackStats(remoteAudioTrackStats);
        assertTrue(0 < remoteAudioTrackStats.audioLevel);
        assertTrue(0 < remoteAudioTrackStats.jitter);

        // Check RemoteVideoTrackStats
        RemoteVideoTrackStats remoteVideoTrackStats = statsReport.getRemoteVideoTrackStats().get(0);
        checkBaseTrackStats(remoteVideoTrackStats);
        checkTrackStats(remoteVideoTrackStats);
        assertTrue(0 < remoteVideoTrackStats.frameRate);
        assertTrue(0 < remoteVideoTrackStats.dimensions.width);
        assertTrue(0 < remoteVideoTrackStats.dimensions.height);
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
        assertEquals(1, aliceRoom.getRemoteParticipants().size());

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
        Thread.sleep(2000);

        // call get stats after room has been disconnected
        aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertFalse(aliceStatsListener.onStatsLatch.await(5, TimeUnit.SECONDS));
    }

    private Room createRoom(String token,
                            CallbackHelper.FakeRoomListener listener,
                            String roomName) throws InterruptedException {
        return createRoom(token, listener, roomName, null, null);
    }

    private Room createRoom(String token,
                            CallbackHelper.FakeRoomListener listener,
                            String roomName,
                            List<LocalAudioTrack> audioTracks) throws InterruptedException {
        return createRoom(token, listener, roomName, audioTracks, null);
    }

    private Room createRoom(String token,
                            CallbackHelper.FakeRoomListener listener,
                            String roomName,
                            @Nullable List<LocalAudioTrack> audioTracks,
                            @Nullable List<LocalVideoTrack> videoTracks)
            throws InterruptedException {
        listener.onConnectedLatch = new CountDownLatch(1);

        if (audioTracks == null) {
           audioTracks = new ArrayList<>();
        }
        if (videoTracks == null) {
           videoTracks = new ArrayList<>();
        }
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(audioTracks)
                .videoTracks(videoTracks)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, listener);
        assertTrue(listener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        return room;
    }

    private void roomTearDown(Room room) throws InterruptedException {
        if (room != null && room.getState() != RoomState.DISCONNECTED) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onDisconnectedLatch = new CountDownLatch(1);
            room.disconnect();
            roomListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS);
        }
    }

    private void expectStatsReportTracksSize(StatsReport statsReport, int localAudioTrackSize,
                                             int localVideoTrackSize, int audioTrackSize,
                                             int videoTrackSize) {

        assertEquals(localAudioTrackSize, statsReport.getLocalAudioTrackStats().size());
        assertEquals(localVideoTrackSize, statsReport.getLocalVideoTrackStats().size());
        assertEquals(audioTrackSize, statsReport.getRemoteAudioTrackStats().size());
        assertEquals(videoTrackSize, statsReport.getRemoteVideoTrackStats().size());
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

    private void checkTrackStats(RemoteTrackStats stats) {
        assertTrue(0 < stats.bytesReceived);
        assertTrue(0 < stats.packetsReceived);
    }
}
