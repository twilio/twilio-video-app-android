package com.twilio.video;

import static com.twilio.video.TestUtils.ICE_TIMEOUT;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.Topology;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@LargeTest
public class ReconnectingTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{Topology.P2P}, {Topology.GROUP}});
    }

    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    private final Topology topology;

    public ReconnectingTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        super.baseSetup(topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canPublishTracksWhileReconnecting() throws InterruptedException {
        CountDownLatch videoTrackPublished = new CountDownLatch(1),
                audioTrackPublished = new CountDownLatch(1),
                dataTrackPublished = new CountDownLatch(1);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();

        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        aliceLocalDataTrack = LocalDataTrack.create(mediaTestActivity);

        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectingLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectedLatch = new CountDownLatch(1);

        aliceRoom
                .getLocalParticipant()
                .setListener(
                        new LocalParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    LocalParticipant localParticipant,
                                    LocalAudioTrackPublication localAudioTrackPublication) {
                                audioTrackPublished.countDown();
                            }

                            @Override
                            public void onAudioTrackPublicationFailed(
                                    LocalParticipant localParticipant,
                                    LocalAudioTrack localAudioTrack,
                                    TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onVideoTrackPublished(
                                    LocalParticipant localParticipant,
                                    LocalVideoTrackPublication localVideoTrackPublication) {
                                videoTrackPublished.countDown();
                            }

                            @Override
                            public void onVideoTrackPublicationFailed(
                                    LocalParticipant localParticipant,
                                    LocalVideoTrack localVideoTrack,
                                    TwilioException twilioException) {
                                fail();
                            }

                            @Override
                            public void onDataTrackPublished(
                                    LocalParticipant localParticipant,
                                    LocalDataTrackPublication localDataTrackPublication) {
                                dataTrackPublished.countDown();
                            }

                            @Override
                            public void onDataTrackPublicationFailed(
                                    LocalParticipant localParticipant,
                                    LocalDataTrack localDataTrack,
                                    TwilioException twilioException) {
                                fail();
                            }
                        });
        // Simulate network loss
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_LOST);
        assertTrue(
                aliceRoomListener.onReconnectingLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        aliceRoom.getLocalParticipant().publishTrack(aliceLocalVideoTrack);
        aliceRoom.getLocalParticipant().publishTrack(aliceLocalAudioTrack);
        aliceRoom.getLocalParticipant().publishTrack(aliceLocalDataTrack);

        // Simulate network connectivity restored
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);
        assertTrue(
                aliceRoomListener.onReconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Validate that track published callbacks were received
        assertTrue(videoTrackPublished.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(audioTrackPublished.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(dataTrackPublished.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void canReceiveRemoteParticipantConnectivityEventsWhileReconnecting()
            throws InterruptedException {
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);

        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectingLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectedLatch = new CountDownLatch(1);

        // Validate RemoteParticipant disconnected event while in Reconnecting
        // Simulate network loss
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_LOST);
        assertTrue(
                aliceRoomListener.onReconnectingLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bobRoom.disconnect();
        // Simulate network connectivity restored
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);

        assertTrue(
                aliceRoomListener.onReconnectingLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                aliceRoomListener.onParticipantDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                aliceRoomListener.onReconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Validate RemoteParticipant connected event while in Reconnecting
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_LOST);

        IceOptions iceOptions =
                new IceOptions.Builder()
                        .abortOnIceServersTimeout(true)
                        .iceServersTimeout(ICE_TIMEOUT)
                        .build();
        ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(bobToken)
                        .roomName(testRoomName)
                        .iceOptions(iceOptions)
                        .build();
        bobRoom = connect(bobConnectOptions, bobRoomListener);
        assertTrue(
                bobRoomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);
        aliceRoomListener.onReconnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectedLatch.await(
                TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS);
        assertTrue(
                aliceRoomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }
}
