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
import static org.junit.Assume.assumeFalse;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@NetworkTest
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
    private LocalAudioTrack aliceLocalAudioTrack;
    private LocalVideoTrack bobLocalVideoTrack;
    private LocalAudioTrack bobLocalAudioTrack;
    private String roomName;
    private VideoRoom videoRoom;
    private final Topology topology;

    public IceTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomName = random(Constants.ROOM_NAME_LENGTH);
        videoRoom = RoomUtils.createRoom(roomName, topology);
        assertNotNull(videoRoom);
        aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE);
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB);
    }

    @After
    public void teardown() {
        RoomUtils.completeRoom(videoRoom);
        if (aliceLocalAudioTrack != null) {
            aliceLocalAudioTrack.release();
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
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
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
                    public void onConnected(@NonNull Room room) {
                        fail();
                    }

                    @Override
                    public void onConnectFailure(
                            @NonNull Room room, @NonNull TwilioException twilioException) {
                        assertEquals(
                                TwilioException.CONFIGURATION_ACQUIRE_FAILED_EXCEPTION,
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
                    public void onDisconnected(
                            @NonNull Room room, @Nullable TwilioException twilioException) {
                        fail();
                    }

                    @Override
                    public void onParticipantConnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                        fail();
                    }

                    @Override
                    public void onParticipantDisconnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                        fail();
                    }

                    @Override
                    public void onDominantSpeakerChanged(
                            @NonNull Room room, @Nullable RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(@NonNull Room room) {
                        fail();
                    }

                    @Override
                    public void onRecordingStopped(@NonNull Room room) {
                        fail();
                    }
                });
        assertTrue(connectFailure.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
    }

    @Test
    public void shouldConnectWithWrongIceServers() throws InterruptedException {
        /*
         * Attempting to connect with incorrect ICE servers may result in a failure to connect in
         * FTL restrictive network.
         */
        assumeFalse(TestUtils.isFTL(mediaTestActivity));
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
        assertTrue(
                roomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        room.disconnect();
        assertTrue(
                roomListener.onDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
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
        boolean isConnected =
                roomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS);

        if (isConnected) {
            room.disconnect();
            roomListener.onDisconnectedLatch.await(
                    TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS);
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
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT * 2)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();

        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        boolean isConnected =
                roomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS);
        assertTrue(isConnected);
        room.disconnect();
        assertTrue(
                roomListener.onDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        RoomUtils.completeRoom(room);
    }

    @Test
    public void shouldConnectWithValidStunServers() throws InterruptedException {
        /*
         * Attempting to connect with only stun servers may result in a failure to connect in FTL
         * restrictive network.
         */
        assumeFalse(TestUtils.isFTL(mediaTestActivity));
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
        assertTrue(
                roomListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        aliceRoom.disconnect();
        assertTrue(
                roomListener.onDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        RoomUtils.completeRoom(aliceRoom);
    }

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
                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
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
        assertTrue(
                aliceListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

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
        CallbackHelper.FakeRemoteParticipantListener remoteParticipantListener =
                new CallbackHelper.FakeRemoteParticipantListener();
        remoteParticipantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        remoteParticipantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        Room bobRoom = Video.connect(mediaTestActivity, connectOptions, bobListener);
        assertTrue(
                bobListener.onConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                aliceListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        aliceRoom.getRemoteParticipants().get(0).setListener(remoteParticipantListener);
        assertTrue(
                remoteParticipantListener.onSubscribedToAudioTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                remoteParticipantListener.onSubscribedToVideoTrackLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        aliceListener.onDisconnectedLatch = new CountDownLatch(1);
        bobListener.onDisconnectedLatch = new CountDownLatch(1);
        aliceRoom.disconnect();
        bobRoom.disconnect();
        assertTrue(
                aliceListener.onDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                bobListener.onDisconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        RoomUtils.completeRoom(aliceRoom);
    }
}
