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

import static com.twilio.video.TestUtils.ICE_TIMEOUT;
import static com.twilio.video.TestUtils.SIP_TIMEOUT;
import static com.twilio.video.TestUtils.SMALL_WAIT;
import static com.twilio.video.TestUtils.STATE_TRANSITION_TIMEOUT;
import static com.twilio.video.util.VideoAssert.assertIsParticipantSid;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.twilioapi.model.VideoRoom;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@LargeTest
public class RoomTopologyParameterizedTest extends BaseVideoTest {
    @Parameterized.Parameters(name = "Topology: {0}, enableRecording: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {Topology.P2P, false},
                    {Topology.GROUP, false},
                    {Topology.GROUP, true}
                });
    }

    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    private MediaTestActivity mediaTestActivity;
    private String identity;
    private String token;
    private String roomName;
    private final CallbackHelper.FakeRoomListener roomListener =
            new CallbackHelper.FakeRoomListener();
    private Room room;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private final Topology topology;
    private final boolean enableRecording;
    private VideoRoom videoRoom;

    public RoomTopologyParameterizedTest(Topology topology, boolean enableRecording) {
        this.topology = topology;
        this.enableRecording = enableRecording;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        identity = Constants.PARTICIPANT_ALICE;
        roomName = random(Constants.ROOM_NAME_LENGTH);
        videoRoom = RoomUtils.createRoom(roomName, topology, enableRecording);
        assertNotNull(videoRoom);
        token = CredentialsUtils.getAccessToken(identity, topology);
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
        if (videoRoom != null) {
            RoomUtils.completeRoom(videoRoom);
        }
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
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .abortOnIceServersTimeout(true)
                        .iceServersTimeout(ICE_TIMEOUT)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .audioTracks(Collections.singletonList(localAudioTrack))
                        .videoTracks(Collections.singletonList(localVideoTrack))
                        .iceOptions(iceOptions)
                        .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertNull(room.getLocalParticipant());
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        assertEquals(identity, localParticipant.getIdentity());
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0).getAudioTrack());
        assertEquals(1, localParticipant.getLocalAudioTracks().size());
        assertEquals(
                localAudioTrack,
                localParticipant.getLocalAudioTracks().get(0).getLocalAudioTrack());
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0).getVideoTrack());
        assertEquals(1, localParticipant.getLocalVideoTracks().size());
        assertEquals(
                localVideoTrack,
                localParticipant.getLocalVideoTracks().get(0).getLocalVideoTrack());
        assertNotNull(localParticipant.getSid());
        assertTrue(!localParticipant.getSid().isEmpty());
        assertIsParticipantSid(localParticipant.getSid());
    }

    @Test
    public void shouldReconnect() throws InterruptedException {
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        for (int i = 0; i < 5; i++) {
            roomListener.onConnectedLatch = new CountDownLatch(1);
            roomListener.onDisconnectedLatch = new CountDownLatch(1);

            room = Video.connect(mediaTestActivity, connectOptions, roomListener);
            assertTrue(
                    roomListener.onConnectedLatch.await(
                            STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
            assertEquals(Room.State.CONNECTED, room.getState());
            room.disconnect();
            assertTrue(
                    roomListener.onDisconnectedLatch.await(
                            STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
            assertEquals(Room.State.DISCONNECTED, room.getState());

            Thread.sleep(1000);
        }
    }

    @Test
    public void shouldTriggerOnReconnectingOnNetworkChange() throws InterruptedException {
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onReconnectingLatch = new CountDownLatch(1);
        roomListener.onReconnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        // Connect to room
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.CONNECTED, room.getState());

        // Fire {@link Video.NetworkChangeEvent.CONNECTION_CHANGED} event
        // Expected flow: Connected -> Reconnecting -> Connected
        room.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);
        assertTrue(
                roomListener.onReconnectingLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.RECONNECTING, room.getState());
        assertEquals(
                TwilioException.SIGNALING_CONNECTION_DISCONNECTED_EXCEPTION,
                roomListener.getTwilioException().getCode());
        assertTrue(
                roomListener.onReconnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.CONNECTED, room.getState());
    }

    @Test
    public void shouldReconnectAfterMultipleNetworkChanges() throws InterruptedException {
        final int NETWORK_CHANGES = 4;
        roomListener.onConnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        // Connect to room
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.CONNECTED, room.getState());

        for (int i = 0; i < NETWORK_CHANGES; i++) {
            roomListener.onReconnectingLatch = new CountDownLatch(1);
            roomListener.onReconnectedLatch = new CountDownLatch(1);

            // Fire {@link Video.NetworkChangeEvent.CONNECTION_CHANGED} event
            // Expected flow: Connected -> Reconnecting -> Connected
            room.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);
            assertTrue(
                    roomListener.onReconnectingLatch.await(
                            STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
            assertEquals(Room.State.RECONNECTING, room.getState());
            assertEquals(
                    TwilioException.SIGNALING_CONNECTION_DISCONNECTED_EXCEPTION,
                    roomListener.getTwilioException().getCode());
            assertTrue(
                    roomListener.onReconnectedLatch.await(
                            STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
            assertEquals(Room.State.CONNECTED, room.getState());
            Thread.sleep(SMALL_WAIT);
        }
    }

    @Test
    public void shouldDisconnectDuringNetworkTimeoutAfterSuccessfulReconnect()
            throws InterruptedException {
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onReconnectingLatch = new CountDownLatch(1);
        roomListener.onReconnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        // Connect to room
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.CONNECTED, room.getState());

        // Fire {@link Video.NetworkChangeEvent.CONNECTION_CHANGED} event
        // Expected flow: Connected -> Reconnecting -> Connected
        room.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);
        assertTrue(
                roomListener.onReconnectingLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.RECONNECTING, room.getState());
        assertEquals(
                TwilioException.SIGNALING_CONNECTION_DISCONNECTED_EXCEPTION,
                roomListener.getTwilioException().getCode());
        assertTrue(
                roomListener.onReconnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.CONNECTED, room.getState());

        // Fire {@link Video.NetworkChangeEvent.CONNECTION_LOST} event
        // Expected flow: Connected -> Reconnecting -> Disconnected
        roomListener.onReconnectingLatch = new CountDownLatch(1);
        room.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_LOST);
        assertTrue(
                roomListener.onReconnectingLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.RECONNECTING, room.getState());
        assertEquals(
                TwilioException.SIGNALING_CONNECTION_DISCONNECTED_EXCEPTION,
                roomListener.getTwilioException().getCode());
        assertTrue(roomListener.onDisconnectedLatch.await(SIP_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.DISCONNECTED, room.getState());
    }

    @Test
    public void shouldTryToReconnectAndTransitionToDisconnectedIfNetworkUnavailable()
            throws InterruptedException {
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onReconnectingLatch = new CountDownLatch(1);
        roomListener.onReconnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        // Connect to room
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.CONNECTED, room.getState());

        // Fire {@link Video.NetworkChangeEvent.CONNECTION_LOST} event
        // Expected flow: Connected -> Reconnecting -> Disconnected
        room.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_LOST);
        assertTrue(
                roomListener.onReconnectingLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.RECONNECTING, room.getState());
        assertEquals(
                TwilioException.SIGNALING_CONNECTION_DISCONNECTED_EXCEPTION,
                roomListener.getTwilioException().getCode());
        assertTrue(roomListener.onDisconnectedLatch.await(SIP_TIMEOUT, TimeUnit.SECONDS));
        assertEquals(Room.State.DISCONNECTED, room.getState());
    }

    @Test
    public void shouldFailToConnectWithInvalidToken() throws InterruptedException {
        String invalidToken = "invalid token";
        ConnectOptions connectOptions = new ConnectOptions.Builder(invalidToken).build();
        final CountDownLatch connectFailure = new CountDownLatch(1);
        Video.connect(
                mediaTestActivity,
                connectOptions,
                new Room.Listener() {
                    @Override
                    public void onConnected(Room room) {
                        fail();
                    }

                    @Override
                    public void onConnectFailure(Room room, TwilioException twilioException) {
                        assertEquals(
                                TwilioException.ACCESS_TOKEN_INVALID_EXCEPTION,
                                twilioException.getCode());
                        connectFailure.countDown();
                    }

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {
                        fail();
                    }

                    @Override
                    public void onReconnected(@NonNull Room room) {
                        fail();
                    }

                    @Override
                    public void onDisconnected(Room room, TwilioException twilioException) {
                        fail();
                    }

                    @Override
                    public void onParticipantConnected(
                            Room room, RemoteParticipant remoteParticipant) {
                        fail();
                    }

                    @Override
                    public void onParticipantDisconnected(
                            Room room, RemoteParticipant remoteParticipant) {
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
        assertTrue(connectFailure.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReturnValidRecordingState() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertNull(room.getLocalParticipant());
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        if ((topology == Topology.GROUP && enableRecording)
                || (topology == Topology.GROUP_SMALL && enableRecording)) {
            Assert.assertTrue(room.isRecording());
        } else {
            assertFalse(room.isRecording());
        }

        room.disconnect();

        // Wait for disconnect and validate recording state
        assertTrue(
                roomListener.onDisconnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        Assert.assertFalse(room.isRecording());
    }

    @Test
    public void shouldDisconnectDuplicateParticipant() throws InterruptedException {
        // Connect first bobRemoteParticipant
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        final CountDownLatch connectedLatch = new CountDownLatch(1);
        final CountDownLatch disconnectedLatch = new CountDownLatch(1);
        Video.connect(
                mediaTestActivity,
                connectOptions,
                new Room.Listener() {
                    @Override
                    public void onConnected(Room room) {
                        connectedLatch.countDown();
                    }

                    @Override
                    public void onConnectFailure(Room room, TwilioException twilioException) {
                        fail();
                    }

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {
                        fail();
                    }

                    @Override
                    public void onReconnected(@NonNull Room room) {
                        fail();
                    }

                    @Override
                    public void onDisconnected(Room room, TwilioException twilioException) {
                        assertEquals(
                                TwilioException.PARTICIPANT_DUPLICATE_IDENTITY_EXCEPTION,
                                twilioException.getCode());
                        disconnectedLatch.countDown();
                    }

                    @Override
                    public void onParticipantConnected(Room room, RemoteParticipant participant) {
                        fail();
                    }

                    @Override
                    public void onParticipantDisconnected(
                            Room room, RemoteParticipant participant) {
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
        assertTrue(connectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Connect second bobRemoteParticipant
        connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // First bobRemoteParticipant should get disconnected
        assertTrue(disconnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReceiveDisconnectedCallbackWhenCompleted() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        // Connect to room
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token).roomName(roomName).iceOptions(iceOptions).build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Complete room via REST API
        VideoRoom videoRoom = RoomUtils.completeRoom(room);

        // Validate status is completed
        assertEquals("completed", videoRoom.getStatus());

        // Validate disconnected callback is received
        assertTrue(
                roomListener.onDisconnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        // Validate we receive a distinct error code when the room is completed
        assertEquals(
                roomListener.getTwilioException().getCode(),
                TwilioException.ROOM_ROOM_COMPLETED_EXCEPTION);
    }
}
