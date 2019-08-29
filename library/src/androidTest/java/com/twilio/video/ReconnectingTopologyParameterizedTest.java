package com.twilio.video;

import static com.twilio.video.TestUtils.ICE_TIMEOUT;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.Manifest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.testcategories.NetworkTest;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.Sequence;
import com.twilio.video.util.Topology;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@NetworkTest
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
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();

        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        aliceLocalDataTrack = LocalDataTrack.create(mediaTestActivity);

        /*
         * Define the expected sequence. Tracks can be published in any order so long as they occur
         * after reconnecting -> reconnected
         */
        final List<String> expectedSequence =
                Arrays.asList(
                        "onReconnecting",
                        "onReconnected",
                        "onTrackPublished",
                        "onTrackPublished",
                        "onTrackPublished");
        final Sequence reconnectingEventSequence = new Sequence(expectedSequence);
        aliceRoomListener.sequence = reconnectingEventSequence;
        aliceRoomListener.onReconnectingLatch = new CountDownLatch(1);

        aliceRoom
                .getLocalParticipant()
                .setListener(
                        new LocalParticipant.Listener() {
                            @Override
                            public void onAudioTrackPublished(
                                    LocalParticipant localParticipant,
                                    LocalAudioTrackPublication localAudioTrackPublication) {
                                reconnectingEventSequence.addEvent("onTrackPublished");
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
                                reconnectingEventSequence.addEvent("onTrackPublished");
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
                                reconnectingEventSequence.addEvent("onTrackPublished");
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

        // Validate the sequence occurred as expected
        reconnectingEventSequence.assertSequenceOccurred(
                expectedSequence.size() * TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void canReceiveRemoteParticipantConnectivityEventsWhileReconnecting()
            throws InterruptedException {
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectingLatch = new CountDownLatch(1);
        aliceRoomListener.onReconnectedLatch = new CountDownLatch(1);

        // Validate RemoteParticipant disconnected event while in Reconnecting
        // Simulate network loss
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_LOST);
        assertTrue(
                aliceRoomListener.onReconnectingLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Disconnect bob while reconnecting
        bobRoom.disconnect();

        // Simulate network connectivity restored
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);

        // Wait for events
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
