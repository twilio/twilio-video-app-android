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
import com.twilio.video.util.Topology;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class RoomTopologyParameterizedTest extends BaseClientTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.SFU},
                {Topology.SFU_RECORDING}});
    }

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String identity;
    private String token;
    private String roomName;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private final Topology topology;

    public RoomTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        identity = Constants.PARTICIPANT_ALICE;
        token = CredentialsUtils.getAccessToken(identity, topology);
        roomName = RandUtils.generateRandomString(20);
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
    public void shouldReturnLocalParticipantOnConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertNull(room.getLocalParticipant());
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        assertEquals(identity, localParticipant.getIdentity());
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0));
        assertNotNull(localParticipant.getSid());
        assertTrue(!localParticipant.getSid().isEmpty());
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReconnect() throws InterruptedException {
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .roomName(roomName)
            .build();
        for (int i = 0 ; i < 5 ; i++) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onConnectedLatch = new CountDownLatch(1);
            roomListener.onDisconnectedLatch = new CountDownLatch(1);

            Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
            assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
            assertEquals(RoomState.CONNECTED, room.getState());

            room.disconnect();
            assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
            assertEquals(RoomState.DISCONNECTED, room.getState());

            Thread.sleep(1000);
        }
    }

    @Test
    public void shouldFailToConnectWithInvalidToken() throws InterruptedException {
        String invalidToken = "invalid token";
        ConnectOptions connectOptions = new ConnectOptions.Builder(invalidToken).build();
        final CountDownLatch connectFailure = new CountDownLatch(1);
        Video.connect(mediaTestActivity, connectOptions, new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                fail();
            }

            @Override
            public void onConnectFailure(Room room, TwilioException twilioException) {
                assertEquals(TwilioException.ACCESS_TOKEN_INVALID_EXCEPTION, twilioException.getCode());
                connectFailure.countDown();
            }

            @Override
            public void onDisconnected(Room room, TwilioException twilioException) {
                fail();
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                fail();
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                fail();
            }

            @Override
            public void onRecordingStarted(Room room) {
                fail();
            }

            @Override
            public void onRecordingStopped(Room room) {
                fail();
            }
        });
        assertTrue(connectFailure.await(10, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReturnValidRecordingState() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertNull(room.getLocalParticipant());
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        if(topology == Topology.P2P || topology == Topology.SFU) {
           Assert.assertFalse(room.isRecording());
        } else {
            /*
             * Making an assumption that other topologies, will have recording enabled by default.
             * This assumption is subject to change and we will have to update this test
             * accordingly.
             */
            Assert.assertTrue(room.isRecording());
        }

        room.disconnect();

        // Wait for disconnect and validate recording state
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        Assert.assertFalse(room.isRecording());
    }

    @Test
    public void shouldDisconnectDuplicateParticipant() throws InterruptedException {
        // Connect first participant
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .roomName(roomName)
            .build();
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        Room room1 = Video.connect(mediaTestActivity, connectOptions, new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                connectedLatch.countDown();
            }

            @Override
            public void onConnectFailure(Room room, TwilioException twilioException) {
                fail();
            }

            @Override
            public void onDisconnected(Room room, TwilioException twilioException) {
                assertEquals(TwilioException.PARTICIPANT_DUPLICATE_IDENTITY_EXCEPTION, twilioException.getCode());
                disconnectedLatch.countDown();
            }

            @Override
            public void onParticipantConnected(Room room, Participant participant) {
                fail();
            }

            @Override
            public void onParticipantDisconnected(Room room, Participant participant) {
                fail();
            }

            @Override
            public void onRecordingStarted(Room room) {
                fail();
            }

            @Override
            public void onRecordingStopped(Room room) {
                fail();
            }
        });
        assertTrue(connectedLatch.await(10, TimeUnit.SECONDS));

        // Connect second participant
        connectOptions = new ConnectOptions.Builder(token)
            .roomName(roomName)
            .build();
        CallbackHelper.FakeRoomListener room2Listener = new CallbackHelper.FakeRoomListener();
        room2Listener.onConnectedLatch = new CountDownLatch(1);
        room2Listener.onDisconnectedLatch = new CountDownLatch(1);
        Room room2 = Video.connect(mediaTestActivity, connectOptions, room2Listener);
        assertTrue(room2Listener.onConnectedLatch.await(10, TimeUnit.SECONDS));

        // First participant should get disconnected
        assertTrue(disconnectedLatch.await(10, TimeUnit.SECONDS));

        // Disconnect second participant
        room2.disconnect();
        assertTrue(room2Listener.onDisconnectedLatch.await(10, TimeUnit.SECONDS));
    }

}
