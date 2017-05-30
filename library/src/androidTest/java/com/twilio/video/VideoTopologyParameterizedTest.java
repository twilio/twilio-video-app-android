package com.twilio.video;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class VideoTopologyParameterizedTest extends BaseClientTest {
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
    private String token;
    private String roomName;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private CallbackHelper.FakeRoomListener roomListener;
    private final Topology topology;

    public VideoTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomListener = new CallbackHelper.FakeRoomListener();
        PermissionUtils.allowPermissions(mediaTestActivity);
        roomName = RandUtils.generateRandomString(20);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        token = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        Video.setLogLevel(LogLevel.ALL);
    }

    @After
    public void teardown() {
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void connect_shouldConnectToRoom() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getSid(), room.getName());
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore("Disconnecting while connecting results in native crash. See GSDK-1153")
    public void disconnect_canDisconnectBeforeConnectingToRoom() throws InterruptedException {
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowAudioTracks() throws InterruptedException {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        List<LocalAudioTrack> localAudioTrackList =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTrackList)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        // Validate tracks in local participant
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getAudioTracks().get(0));
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0));
        assertTrue(localParticipant.removeAudioTrack(localAudioTrack));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowVideoTracks() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        List<LocalVideoTrack> localVideoTrackList =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTrackList)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getVideoTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0));
        assertTrue(localParticipant.removeVideoTrack(localVideoTrack));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowAudioAndVideoTracks() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        List<LocalAudioTrack> localAudioTrackList =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};
        List<LocalVideoTrack> localVideoTrackList =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTrackList)
                .videoTracks(localVideoTrackList)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getAudioTracks().get(0));
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0));
        assertNotNull(localParticipant.getVideoTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0));
        assertTrue(localParticipant.removeAudioTrack(localAudioTrack));
        assertTrue(localParticipant.removeVideoTrack(localVideoTrack));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldFailToConnectWithBadToken() throws InterruptedException {
        roomListener.onConnectFailureLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder("bad token")
            .roomName(roomName)
            .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectFailureLatch.await(20, TimeUnit.SECONDS));
        assertEquals(roomListener.getTwilioException().getCode(),
            TwilioException.ACCESS_TOKEN_INVALID_EXCEPTION);
        assertNotNull(roomListener.getTwilioException().getMessage());
    }

    @Test(expected = IllegalStateException.class)
    public void connect_shouldShouldFailIfLocalVideoTrackReleasedBeforeConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .roomName(roomName)
            .videoTracks(localVideoTracks)
            .build();
        localVideoTrack.release();
        Video.connect(mediaTestActivity, connectOptions, roomListener);
    }

    @Test(expected = IllegalStateException.class)
    public void connect_shouldShouldFailIfLocalAudioTrackReleasedBeforeConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .roomName(roomName)
            .audioTracks(localAudioTracks)
            .build();
        localAudioTrack.release();
        Video.connect(mediaTestActivity, connectOptions, roomListener);
    }

    @Test
    public void connect_shouldAllowLocalVideoTrackToBeReleasedWhileConnecting()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTracks)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);

        // Add sleep to ensure that connect has started
        Thread.sleep(200);

        localVideoTrack.release();
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowLocalAudioTrackToBeReleasedWhileConnecting()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTracks)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);

        // Add sleep to ensure that connect has started
        Thread.sleep(200);

        localAudioTrack.release();
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowLocalVideoTrackToBeReleasedAfterConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTracks)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        localVideoTrack.release();
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowLocalAudioTrackToBeReleasedAfterConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTracks)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        localAudioTrack.release();
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

}
