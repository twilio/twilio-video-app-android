package com.twilio.video;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.TrackContainer;
import com.twilio.video.testcategories.NetworkTest;
import com.twilio.video.twilioapi.model.VideoRoom;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@NetworkTest
@RunWith(JUnitParamsRunner.class)
@LargeTest
public class SignalingRegionParameterizedTests extends BaseVideoTest {

    enum MediaRegion {
        GLL("gll"),
        BR1("br1"),
        US1("us1");

        private final String value;

        MediaRegion(String value) {
            this.value = value;
        }
    }

    enum SignalingRegion {
        GLL("gll"),
        BR1("br1"),
        US1("us1");

        private final String value;

        SignalingRegion(String value) {
            this.value = value;
        }
    }

    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    public static final String ALICE = "ALICE";
    public static final String BOB = "BOB";

    private MediaTestActivity mediaTestActivity;
    private Map<String, String> identities = new HashMap<>();
    private Map<String, String> tokens = new HashMap<>();
    private Map<String, TrackContainer> trackMap = new HashMap<>();
    private String roomName;
    private VideoRoom videoRoom;
    private String region;

    public SignalingRegionParameterizedTests() {}

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();

        identities.put(ALICE, Constants.PARTICIPANT_ALICE);
        identities.put(BOB, Constants.PARTICIPANT_BOB);

        roomName = random(Constants.ROOM_NAME_LENGTH);
        tokens.put(ALICE, CredentialsUtils.getAccessToken(identities.get(ALICE), Topology.GROUP));
        tokens.put(BOB, CredentialsUtils.getAccessToken(identities.get(BOB), Topology.GROUP));

        Video.setModuleLogLevel(LogModule.SIGNALING, LogLevel.ALL);
        Video.setModuleLogLevel(LogModule.CORE, LogLevel.ALL);
        Video.setModuleLogLevel(LogModule.WEBRTC, LogLevel.ALL);
        Video.setModuleLogLevel(LogModule.PLATFORM, LogLevel.ALL);
    }

    @After
    public void teardown() throws InterruptedException {
        // Teardown
        for (HashMap.Entry<String, TrackContainer> entry : trackMap.entrySet()) {
            TrackContainer trackContainer = entry.getValue();
            trackContainer.release();
        }
        trackMap.clear();

        if (videoRoom != null) {
            RoomUtils.completeRoom(videoRoom);
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    @SdkSuppress(minSdkVersion = 20) // TLS 1.1 is deprecated for the Twilio REST API
    @Parameters(source = SignalingRegion.class)
    public void shouldConnectToRegion(SignalingRegion region) throws InterruptedException {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch disconnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .abortOnIceServersTimeout(true)
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(tokens.get(ALICE))
                        .region(region.value)
                        .iceOptions(iceOptions)
                        .build();
        Room room =
                Video.connect(
                        mediaTestActivity,
                        connectOptions,
                        new Room.Listener() {
                            @Override
                            public void onConnected(@NonNull Room room) {
                                connectedLatch.countDown();
                            }

                            @Override
                            public void onConnectFailure(
                                    @NonNull Room room, @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onReconnecting(
                                    @NonNull Room room, @NonNull TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onReconnected(@NonNull Room room) {}

                            @Override
                            public void onDisconnected(
                                    @NonNull Room room, @Nullable TwilioException twilioException) {
                                disconnectedLatch.countDown();
                            }

                            @Override
                            public void onParticipantConnected(
                                    @NonNull Room room,
                                    @NonNull RemoteParticipant remoteParticipant) {}

                            @Override
                            public void onParticipantDisconnected(
                                    @NonNull Room room,
                                    @NonNull RemoteParticipant remoteParticipant) {}

                            @Override
                            public void onRecordingStarted(@NonNull Room room) {}

                            @Override
                            public void onRecordingStopped(@NonNull Room room) {}
                        });
        assertTrue(connectedLatch.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        room.disconnect();
        assertTrue(disconnectedLatch.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    /*
     Supported media regions can be found here https://www.twilio.com/docs/video/ip-address-whitelisting#group-rooms-media-servers
    */
    @Test
    @SdkSuppress(minSdkVersion = 20) // TLS 1.1 is deprecated for the Twilio REST API
    @Parameters({
        "GLL, GLL, GLL",
        "GLL, BR1, US1",
        "BR1, GLL, GLL",
    })
    public void shouldAllowConnectFromDifferentRegionsWithTracks(
            MediaRegion mediaRegion, SignalingRegion aliceRegion, SignalingRegion bobRegion)
            throws InterruptedException {
        final int DATA_MESSAGES = 1;
        String uniqueRoomName = RandomStringUtils.random(12);
        videoRoom =
                RoomUtils.createRoom(
                        uniqueRoomName,
                        Topology.GROUP,
                        false,
                        mediaRegion.value,
                        mediaRegion.value,
                        null);
        // Omit video and data tracks to avoid media related time outs
        TrackContainer aliceTrackContainer =
                new TrackContainer(mediaTestActivity, false, true, false);

        trackMap.put(ALICE, aliceTrackContainer);

        TrackContainer bobTrackContainer =
                new TrackContainer(mediaTestActivity, false, true, false);
        trackMap.put(BOB, bobTrackContainer);

        CountDownLatch aliceOnConnectedLatch = new CountDownLatch(1);
        CountDownLatch aliceOnDisconnectedLatch = new CountDownLatch(1);
        CountDownLatch bobOnConnectedLatch = new CountDownLatch(1);
        CountDownLatch bobOnDisconnectedLatch = new CountDownLatch(1);

        CountDownLatch aliceSubscribedToTracks = new CountDownLatch(1);
        CountDownLatch bobSubscribedToTracks = new CountDownLatch(1);

        RemoteParticipant.Listener aliceRemoteParticipantListener =
                new RemoteParticipant.Listener() {
                    @Override
                    public void onAudioTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull RemoteAudioTrack remoteAudioTrack) {
                        aliceSubscribedToTracks.countDown();
                    }

                    @Override
                    public void onAudioTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onAudioTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull RemoteAudioTrack remoteAudioTrack) {}

                    @Override
                    public void onVideoTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull RemoteVideoTrack remoteVideoTrack) {
                        aliceSubscribedToTracks.countDown();
                    }

                    @Override
                    public void onVideoTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onVideoTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull RemoteVideoTrack remoteVideoTrack) {}

                    @Override
                    public void onDataTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull RemoteDataTrack remoteDataTrack) {
                        aliceSubscribedToTracks.countDown();
                    }

                    @Override
                    public void onDataTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onDataTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull RemoteDataTrack remoteDataTrack) {
                        remoteDataTrack.setListener(null);
                    }

                    @Override
                    public void onAudioTrackEnabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackDisabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onVideoTrackEnabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackDisabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}
                };
        RemoteParticipant.Listener bobRemoteParticipantListener =
                new RemoteParticipant.Listener() {
                    @Override
                    public void onAudioTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull RemoteAudioTrack remoteAudioTrack) {
                        bobSubscribedToTracks.countDown();
                    }

                    @Override
                    public void onAudioTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onAudioTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull RemoteAudioTrack remoteAudioTrack) {}

                    @Override
                    public void onVideoTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull RemoteVideoTrack remoteVideoTrack) {
                        bobSubscribedToTracks.countDown();
                    }

                    @Override
                    public void onVideoTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onVideoTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull RemoteVideoTrack remoteVideoTrack) {}

                    @Override
                    public void onDataTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull RemoteDataTrack remoteDataTrack) {
                        bobSubscribedToTracks.countDown();
                    }

                    @Override
                    public void onDataTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onDataTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull RemoteDataTrack remoteDataTrack) {
                        remoteDataTrack.setListener(null);
                    }

                    @Override
                    public void onAudioTrackEnabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackDisabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onVideoTrackEnabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackDisabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}
                };

        Room.Listener aliceRoomListener =
                new Room.Listener() {
                    @Override
                    public void onConnected(@NonNull Room room) {
                        aliceOnConnectedLatch.countDown();
                    }

                    @Override
                    public void onConnectFailure(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnected(@NonNull Room room) {}

                    @Override
                    public void onDisconnected(
                            @NonNull Room room, @Nullable TwilioException twilioException) {
                        aliceOnDisconnectedLatch.countDown();
                    }

                    @Override
                    public void onParticipantConnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                        remoteParticipant.setListener(aliceRemoteParticipantListener);
                    }

                    @Override
                    public void onParticipantDisconnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(@NonNull Room room) {}

                    @Override
                    public void onRecordingStopped(@NonNull Room room) {}
                };
        Room.Listener bobRoomListener =
                new Room.Listener() {
                    @Override
                    public void onConnected(@NonNull Room room) {
                        bobOnConnectedLatch.countDown();
                    }

                    @Override
                    public void onConnectFailure(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnected(@NonNull Room room) {}

                    @Override
                    public void onDisconnected(
                            @NonNull Room room, @Nullable TwilioException twilioException) {
                        bobOnDisconnectedLatch.countDown();
                    }

                    @Override
                    public void onParticipantConnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onParticipantDisconnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(@NonNull Room room) {}

                    @Override
                    public void onRecordingStopped(@NonNull Room room) {}
                };

        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();

        ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(tokens.get(ALICE))
                        .roomName(uniqueRoomName)
                        .region(aliceRegion.value)
                        .audioTracks(Collections.singletonList(aliceTrackContainer.getAudioTrack()))
                        .iceOptions(iceOptions)
                        .build();

        Room aliceRoom = Video.connect(mediaTestActivity, aliceConnectOptions, aliceRoomListener);
        assertTrue(
                aliceOnConnectedLatch.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(tokens.get(BOB))
                        .region(bobRegion.value)
                        .roomName(uniqueRoomName)
                        .audioTracks(Collections.singletonList(bobTrackContainer.getAudioTrack()))
                        .iceOptions(iceOptions)
                        .build();

        Room bobRoom = Video.connect(mediaTestActivity, bobConnectOptions, bobRoomListener);

        assertTrue(bobOnConnectedLatch.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bobRoom.getRemoteParticipants().get(0).setListener(bobRemoteParticipantListener);

        assertTrue(
                aliceSubscribedToTracks.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                bobSubscribedToTracks.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        CountDownLatch statsReceived = new CountDownLatch(2);

        // Allow enough time to gather stats
        TestUtils.blockingWait(TestUtils.STATE_TRANSITION_TIMEOUT * 1000);

        aliceRoom.getStats(
                statsReports -> {
                    for (StatsReport statsReport : statsReports) {
                        assertTrue(statsReport.getRemoteAudioTrackStats().get(0).bytesReceived > 0);
                    }
                    statsReceived.countDown();
                });

        bobRoom.getStats(
                statsReports -> {
                    for (StatsReport statsReport : statsReports) {
                        assertTrue(statsReport.getRemoteAudioTrackStats().get(0).bytesReceived > 0);
                    }
                    statsReceived.countDown();
                });

        assertTrue(statsReceived.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        aliceRoom.disconnect();
        bobRoom.disconnect();

        assertTrue(
                aliceOnDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                bobOnDisconnectedLatch.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    @SdkSuppress(minSdkVersion = 20) // TLS 1.1 is deprecated for the Twilio REST API
    @Parameters("fake-region1")
    public void shouldFailIfConnectingWithInvalidRegion(String region) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(tokens.get(ALICE))
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .region(region)
                        .roomName(roomName)
                        .build();
        Video.connect(
                mediaTestActivity,
                connectOptions,
                new Room.Listener() {
                    @Override
                    public void onConnected(@NonNull Room room) {
                        fail();
                    }

                    @Override
                    public void onConnectFailure(
                            @NonNull Room room, @NonNull TwilioException twilioException) {
                        assertEquals(
                                TwilioException.SIGNALING_DNS_RESOLUTION_ERROR_EXCEPTION,
                                twilioException.getCode());
                        latch.countDown();
                    }

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnected(@NonNull Room room) {}

                    @Override
                    public void onDisconnected(
                            @NonNull Room room, @Nullable TwilioException twilioException) {}

                    @Override
                    public void onParticipantConnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onParticipantDisconnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(@NonNull Room room) {}

                    @Override
                    public void onRecordingStopped(@NonNull Room room) {}
                });

        assertTrue(latch.await(TestUtils.INVALID_REGION_TIMEOUT, TimeUnit.SECONDS));
    }
}
