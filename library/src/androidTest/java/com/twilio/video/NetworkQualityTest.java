package com.twilio.video;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.testcategories.NetworkTest;
import com.twilio.video.twilioapi.model.VideoRoom;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

@NetworkTest
@LargeTest
public class NetworkQualityTest extends BaseVideoTest {

    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    private MediaTestActivity mediaTestActivity;
    private String roomName;
    private String identity;
    private Room room;
    private final CallbackHelper.FakeRoomListener roomListener =
            new CallbackHelper.FakeRoomListener();
    private VideoRoom videoRoom;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private LocalDataTrack localDataTrack;
    private IceOptions iceOptions;
    private CallbackHelper.FakeLocalParticipantListener localParticipantListener;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        identity = Constants.PARTICIPANT_ALICE;
        roomName = random(Constants.ROOM_NAME_LENGTH);
        iceOptions =
                new IceOptions.Builder()
                        .abortOnIceServersTimeout(true)
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                        .build();
        localParticipantListener = new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onNetworkQualityLevelChangedLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
    }

    @After
    public void teardown() throws InterruptedException {
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
            roomListener.onDisconnectedLatch = new CountDownLatch(1);
            room.disconnect();
            assertTrue(
                    roomListener.onDisconnectedLatch.await(
                            TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        }

        /*
         * After all participants have disconnected complete the room to clean up backend
         * resources.
         */
        if (room != null) {
            RoomUtils.completeRoom(room);
        }
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        if (localDataTrack != null) {
            localDataTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldObserveValidNetworkQualityWithAudioTrack() throws InterruptedException {
        Topology topology = Topology.GROUP;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableNetworkQuality(true)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertNotNull(localAudioTrack);
        assertTrue(localParticipant.publishTrack(localAudioTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedAudioTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNetworkQualityLevel();
    }

    @Test
    public void shouldObserveUnknownNetworkQualityInP2PRoom() throws InterruptedException {
        Topology topology = Topology.P2P;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableNetworkQuality(true)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertNotNull(localAudioTrack);
        assertTrue(localParticipant.publishTrack(localAudioTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedAudioTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertFalse(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        3000, TimeUnit.MILLISECONDS));

        NetworkQualityLevel networkQualityLevel =
                room.getLocalParticipant().getNetworkQualityLevel();
        assertNotNull(networkQualityLevel);
        assertEquals(NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, networkQualityLevel);
    }

    @Test
    public void shouldObserveUnknownNetworkQualityIfDisabledInGroupRoom()
            throws InterruptedException {
        Topology topology = Topology.GROUP;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertNotNull(localAudioTrack);
        assertTrue(localParticipant.publishTrack(localAudioTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedAudioTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertFalse(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        3000, TimeUnit.MILLISECONDS));

        NetworkQualityLevel networkQualityLevel =
                room.getLocalParticipant().getNetworkQualityLevel();
        assertNotNull(networkQualityLevel);
        assertEquals(NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, networkQualityLevel);
    }

    @Test
    public void shouldObserveValidNetworkQualityWithVideoTrack() throws InterruptedException {
        Topology topology = Topology.GROUP;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableNetworkQuality(true)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        assertNotNull(localVideoTrack);
        assertTrue(localParticipant.publishTrack(localVideoTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedVideoTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidNetworkQualityWithDataTrack() throws InterruptedException {
        Topology topology = Topology.GROUP_SMALL;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableNetworkQuality(true)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        assertNotNull(localDataTrack);
        assertTrue(localParticipant.publishTrack(localDataTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedDataTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidNetworkQualityWithAudioVideoAndDataTracks()
            throws InterruptedException {
        Topology topology = Topology.GROUP_SMALL;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableNetworkQuality(true)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        assertNotNull(localVideoTrack);
        assertTrue(localParticipant.publishTrack(localVideoTrack));
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertNotNull(localAudioTrack);
        assertTrue(localParticipant.publishTrack(localAudioTrack));
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        assertNotNull(localDataTrack);
        assertTrue(localParticipant.publishTrack(localDataTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedVideoTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onPublishedAudioTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onPublishedDataTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNetworkQualityLevel();
    }

    private LocalParticipant setupLocalParticipant() {
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        return localParticipant;
    }

    @Ignore(
            "This test is flaky. Calling the room onNetworkChanged method doesn't seem to guarantee a onNetworkLevelChanged invocation")
    @Test
    public void shouldObserveValidNetworkQualityAfterReconnection() throws InterruptedException {
        Topology topology = Topology.GROUP;
        String token = setupRoom(topology);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        roomListener.onReconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .enableNetworkQuality(true)
                        .enableDominantSpeaker(true)
                        .iceOptions(iceOptions)
                        .build();
        connectToRoom(connectOptions);
        LocalParticipant localParticipant = setupLocalParticipant();

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertNotNull(localAudioTrack);
        assertTrue(localParticipant.publishTrack(localAudioTrack));

        // Validate we received callbacks
        assertTrue(
                localParticipantListener.onPublishedAudioTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNetworkQualityLevel();

        room.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);
        localParticipantListener.onNetworkQualityLevelChangedLatch = new CountDownLatch(1);

        assertTrue(
                roomListener.onReconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertTrue(
                localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNetworkQualityLevel();
    }

    private String setupRoom(Topology topology) {
        videoRoom = RoomUtils.createRoom(roomName, topology);
        assertNotNull(videoRoom);
        return CredentialsUtils.getAccessToken(identity, topology);
    }

    private void connectToRoom(ConnectOptions connectOptions) throws InterruptedException {
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(
                roomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    private void assertNetworkQualityLevel() {
        NetworkQualityLevel networkQualityLevel =
                room.getLocalParticipant().getNetworkQualityLevel();
        assertNotNull(networkQualityLevel);
        assertNotEquals(NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, networkQualityLevel);
        List<NetworkQualityLevel> networkQualityLevelEvents =
                localParticipantListener.onNetworkLevelChangedEvents;
        NetworkQualityLevel callbackNetworkQualityLevel =
                networkQualityLevelEvents.get(networkQualityLevelEvents.size() - 1);
        assertNotNull(callbackNetworkQualityLevel);
        assertNotEquals(
                NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, callbackNetworkQualityLevel);
    }

    @Ignore(
            "This test is flaky. Calling the room onNetworkChanged method doesn't seem to guarantee a onNetworkLevelChanged invocation")
    @Test
    public void shouldObserveNetworkQualityLevelZeroAfterNetworkLoss()
            throws InterruptedException {}
}
