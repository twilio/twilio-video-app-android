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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.ServiceTokenUtil;
import com.twilio.video.util.Topology;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@LargeTest
public class IceTopologyParameterizedTest extends BaseVideoTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {{Topology.P2P}, {Topology.GROUP}});
    }

    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    private MediaTestActivity mediaTestActivity;
    private String aliceToken;
    private String bobToken;
    private LocalVideoTrack aliceLocalVideoTrack;
    private LocalAudioTrack aliceLocalAudioTrack;
    private LocalVideoTrack bobLocalVideoTrack;
    private LocalAudioTrack bobLocalAudioTrack;
    private String roomName;
    private final Topology topology;

    public IceTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
    }

    @After
    public void teardown() {
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

    @Test
    public void doTestConnectWithIceServerAbortInvalidToken() throws InterruptedException {
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(3000)
                        .abortOnIceServersTimeout(true)
                        .build();

        String invalidToken = "invalid token";
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(invalidToken).iceOptions(iceOptions).build();
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
                                TwilioException.CONFIGURATION_ACQUIRE_FAILED_EXCEPTION,
                                twilioException.getCode());
                        connectFailure.countDown();
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
        assertTrue(connectFailure.await(10, TimeUnit.SECONDS));
    }

    @Ignore
    @Test
    public void shouldConnectWithWrongIceServers() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        Set<IceServer> iceServers = new HashSet<>();
        iceServers.add(new IceServer("stun:foo.bar.address?transport=udp"));
        iceServers.add(new IceServer("turn:foo.bar.address:3478?transport=udp", "fake", "pass"));

        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServers(iceServers)
                        .iceTransportPolicy(IceTransportPolicy.RELAY)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();

        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        RoomUtils.completeRoom(room);
    }

    @Test
    public void shouldAbortIfIceServerTimeoutFlagIsSet() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onConnectFailureLatch = new CountDownLatch(1);

        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(1)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();

        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        boolean isConnected = roomListener.onConnectedLatch.await(10, TimeUnit.SECONDS);

        if (isConnected) {
            room.disconnect();
            roomListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS);
        } else {
            if (roomListener.getTwilioException() != null) {
                assertTrue(
                        roomListener.getTwilioException().getCode()
                                == TwilioException.CONFIGURATION_ACQUIRE_FAILED_EXCEPTION);
            }
        }
        RoomUtils.completeRoom(room);
    }

    @Test
    public void shouldConnectWithLongTimeoutAndAbort() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onConnectFailureLatch = new CountDownLatch(1);

        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(60000)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();

        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        boolean isConnected = roomListener.onConnectedLatch.await(60, TimeUnit.SECONDS);
        assertTrue(isConnected);
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        RoomUtils.completeRoom(room);
    }

    @Ignore
    @Test
    public void shouldConnectWithValidStunServers() throws InterruptedException {
        Set<IceServer> iceServers = new HashSet<>();
        iceServers.add(new IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun1.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun2.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun3.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun4.l.google.com:19302"));
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServers(iceServers)
                        .iceTransportPolicy(IceTransportPolicy.ALL)
                        .build();
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);

        ConnectOptions connectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .audioTracks(Collections.singletonList(aliceLocalAudioTrack))
                        .build();
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        Room aliceRoom = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        aliceRoom.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        RoomUtils.completeRoom(aliceRoom);
    }

    @Ignore
    @Test
    public void shouldConnectWithValidTurnServers() throws InterruptedException {
        CallbackHelper.FakeRoomListener aliceListener = new CallbackHelper.FakeRoomListener();
        aliceListener.onConnectedLatch = new CountDownLatch(1);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Get ice servers from Twilio Service Token
        Set<IceServer> iceServers = ServiceTokenUtil.getIceServers();

        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServers(iceServers)
                        .iceTransportPolicy(IceTransportPolicy.RELAY)
                        .build();
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .audioTracks(Collections.singletonList(aliceLocalAudioTrack))
                        .build();

        Room aliceRoom = Video.connect(mediaTestActivity, connectOptions, aliceListener);
        assertTrue(aliceListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobLocalVideoTrack =
                LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());

        connectOptions =
                new ConnectOptions.Builder(bobToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .audioTracks(Collections.singletonList(bobLocalAudioTrack))
                        .videoTracks(Collections.singletonList(bobLocalVideoTrack))
                        .build();
        CallbackHelper.FakeRoomListener bobListener = new CallbackHelper.FakeRoomListener();
        bobListener.onConnectedLatch = new CountDownLatch(1);
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        participantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        Room bobRoom = Video.connect(mediaTestActivity, connectOptions, bobListener);
        assertTrue(bobListener.onConnectedLatch.await(10, TimeUnit.SECONDS));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(10, TimeUnit.SECONDS));
        aliceRoom.getRemoteParticipants().get(0).setListener(participantListener);
        assertTrue(participantListener.onSubscribedToAudioTrackLatch.await(10, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToVideoTrackLatch.await(10, TimeUnit.SECONDS));

        aliceListener.onDisconnectedLatch = new CountDownLatch(1);
        bobListener.onDisconnectedLatch = new CountDownLatch(1);
        aliceRoom.disconnect();
        bobRoom.disconnect();
        assertTrue(aliceListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS));
        assertTrue(bobListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS));
        RoomUtils.completeRoom(aliceRoom);
    }
}
