package com.twilio.video;


import android.icu.text.MessagePattern;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.AccessTokenUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StatsTest extends BaseClientTest {

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String aliceIdentity, bobIdentity;
    private String aliceToken, bobToken;
    private VideoClient aliceVideoClient, bobVideoClient;
    private String roomName;
    private Room aliceRoom, bobRoom;
    private LocalMedia aliceLocalMedia, bobLocalMedia;
    private CallbackHelper.FakeRoomListener aliceListener, bobListener;

    private VideoClient createVideoClient() {
        String identity = RandUtils.generateRandomString(10);
        String token = AccessTokenUtils.getAccessToken(identity, BuildConfig.REALM);
        VideoClient videoClient = new VideoClient(mediaTestActivity, token);
        return videoClient;
    }

    private Room createRoom(VideoClient videoClient, CallbackHelper.FakeRoomListener listener,
                            String roomName, LocalMedia localMedia) throws InterruptedException {
        listener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = videoClient.connect(connectOptions, listener);
        assertTrue(listener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        return room;
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
        assertNotEquals("", stats.codecName);
        assertNotEquals("", stats.ssrc);
        assertNotEquals("", stats.trackId);
        // TODO: Packets lost is always 0. Find a way to make test that will excersize this
        assertTrue(0 <= stats.packetsLost);
        assertTrue(0.0 < stats.unixTimestamp);
    }

    private void checkLocalTrackStats(LocalTrackStats stats) {
        assertTrue(0 < stats.bytesSent);
        assertTrue(0 < stats.packetsSent);
        assertTrue(0 < stats.roundTripTime);
    }

    private void checkTrackStats(TrackStats stats) {
        assertTrue(0 < stats.bytesReceived);
        assertTrue(0 < stats.packetsReceived);
        // Always 0. This field will be removed.
        //assertTrue(0 < stats.jitterBuffer);
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        aliceVideoClient = createVideoClient();
        bobVideoClient = createVideoClient();
        roomName = RandUtils.generateRandomString(20);
        aliceListener = new CallbackHelper.FakeRoomListener();
        bobListener = new CallbackHelper.FakeRoomListener();
        aliceLocalMedia = LocalMedia.create(mediaTestActivity);
        bobLocalMedia = LocalMedia.create(mediaTestActivity);
    }

    @After
    public void teardown() {
        aliceLocalMedia.release();
        bobLocalMedia.release();;
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullListener() throws InterruptedException {
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
        aliceRoom.getStats(null);
    }

    @Test
    public void shouldReceiveEmptyStatsInEmptyRoom() throws InterruptedException {
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        assertEquals(0, aliceStatsListener.getStatsReports().size());
    }

    @Test
    public void shouldInvokeListenerOnCallingThread() throws InterruptedException {
        // Connect Alice to room with local audio track only
        aliceLocalMedia.addAudioTrack(true);
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
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
                        statsCallback.countDown();;
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
        aliceLocalMedia.addAudioTrack(true);
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with audio and video track
        bobLocalMedia.addAudioTrack(true);
        bobLocalMedia.addVideoTrack(true, new FakeVideoCapturer());
        bobRoom = createRoom(bobVideoClient, bobListener, roomName, bobLocalMedia);
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
    public void shouldReceiveStatsWhenParticipanAddTrack() throws InterruptedException {
        // Connect Alice to room with local audio track only
        aliceLocalMedia.addAudioTrack(true);
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob without media
        bobRoom = createRoom(bobVideoClient, bobListener, roomName, bobLocalMedia);
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());

        // Add audio track to Bob and check stats
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        Participant bob = aliceRoom.getParticipants().entrySet().iterator().next().getValue();
        bob.getMedia().setListener(mediaListener);

        bobLocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));

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
        LocalVideoTrack bobVideoTrack =
                bobLocalMedia.addVideoTrack(true, new FakeVideoCapturer());
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

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

        // TODO: Commented out until CSDK-1039 is fixed
//        // Remove Bob's video track and check the stats
//        bobLocalMedia.removeVideoTrack(bobVideoTrack);
//        assertTrue(mediaListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
//
//        // let's give peer connection some time to get media flowing
//        Thread.sleep(2000);
//        aliceStatsListener = new CallbackHelper.FakeStatsListener();
//        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
//        aliceRoom.getStats(aliceStatsListener);
//        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
//
//        statsReportList = aliceStatsListener.getStatsReports();
//        assertEquals(1, statsReportList.size());
//        statsReport = statsReportList.get(0);
//        expectStatsReportTracksSize(statsReport, 1, 0, 1, 0);
    }

    @Test
    public void shouldReceiveStatsWhenLocalTrackIsAdded() throws InterruptedException {
        // Connect Alice to room without media
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob without tracks
        bobRoom = createRoom(bobVideoClient, bobListener, roomName, bobLocalMedia);
        assertTrue(aliceListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, aliceRoom.getParticipants().size());

        // Add audio and video track to alice
        aliceLocalMedia.addVideoTrack(true, new FakeVideoCapturer());
        aliceLocalMedia.addAudioTrack(true);

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
        expectStatsReportTracksSize(statsReport, 1, 1, 0, 0);
    }

    @Test
    public void reportShouldHaveNonEmptyValues() throws InterruptedException {
        // Connect Alice to room with both video and audio track
        aliceLocalMedia.addAudioTrack(true);
        aliceLocalMedia.addVideoTrack(true, new FakeVideoCapturer());
        aliceRoom = createRoom(aliceVideoClient, aliceListener, roomName, aliceLocalMedia);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Connect Bob to room with both video and audio track
        bobLocalMedia.addAudioTrack(true);
        bobLocalMedia.addVideoTrack(true, new FakeVideoCapturer());
        bobRoom = createRoom(bobVideoClient, bobListener, roomName, bobLocalMedia);
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
        assertTrue(0 < localAudioTrackStats.audioInputLevel);
        assertTrue(0 < localAudioTrackStats.jitterReceived);
        // TODO: jitterBufferMs will be kicked out from report
        //assertTrue(0 < localAudioTrackStats.jitterBufferMs);
        assertTrue(0 < localAudioTrackStats.roundTripTime);

        // Check LocalVideoTrackStats
        LocalVideoTrackStats localVideoTrackStats = statsReport.getLocalVideoTrackStats().get(0);
        checkBaseTrackStats(localVideoTrackStats);
        checkLocalTrackStats(localVideoTrackStats);
        assertTrue(0 < localVideoTrackStats.capturedFrameRate);
        assertTrue(0 < localVideoTrackStats.captureDimensions.width);
        assertTrue(0 < localVideoTrackStats.captureDimensions.height);
        assertTrue(0 < localVideoTrackStats.sentFrameRate);
        assertTrue(0 < localVideoTrackStats.sentDimensions.width);
        assertTrue(0 < localVideoTrackStats.sentDimensions.height);
        assertTrue(0 < localVideoTrackStats.roundTripTime);

        // Check AudioTrackStats
        AudioTrackStats audioTrackStats = statsReport.getAudioTrackStats().get(0);
        checkBaseTrackStats(audioTrackStats);
        checkTrackStats(audioTrackStats);
        assertTrue(0 < audioTrackStats.audioOutputLevel);
        assertTrue(0 < audioTrackStats.jitterReceived);

        // Check VideoTrackStats
        VideoTrackStats videoTrackStats = statsReport.getVideoTrackStats().get(0);
        checkBaseTrackStats(videoTrackStats);
        checkTrackStats(videoTrackStats);
        assertTrue(0 < videoTrackStats.receivedFrameRate);
        assertTrue(0 < videoTrackStats.receivedDimensions.width);
        assertTrue(0 < videoTrackStats.receivedDimensions.height);
    }



}
