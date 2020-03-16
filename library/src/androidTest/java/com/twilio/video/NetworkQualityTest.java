/*
 * Copyright (C) 2019 Twilio, Inc.
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

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.support.annotation.NonNull;
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
    private VideoRoom videoRoom;

    private NetworkQualityTestParticipant alice;
    private NetworkQualityTestParticipant bob;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomName = random(Constants.ROOM_NAME_LENGTH);

        alice = new NetworkQualityTestParticipant(Constants.PARTICIPANT_ALICE);
        bob = new NetworkQualityTestParticipant(Constants.PARTICIPANT_BOB);
    }

    @After
    public void teardown() throws InterruptedException {
        alice.disconnectRoom();
        bob.disconnectRoom();

        RoomUtils.completeRoom(videoRoom);
        videoRoom = null;

        /*
         * After all participants have disconnected complete the room to clean up backend
         * resources.
         */
        alice.cleanup();
        bob.cleanup();

        assertTrue(MediaFactory.isReleased());
    }

    private void createRoom(Topology topology) {
        videoRoom = RoomUtils.createRoom(roomName, topology);
        assertNotNull(videoRoom);
    }

    @Test
    public void shouldObserveUnknownNetworkQualityInP2PRoom() throws InterruptedException {
        createRoom(Topology.P2P);
        alice.connectToRoom(roomName, true);
        bob.connectToRoom(roomName, true);

        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Waiting for a short bit to ensure no network quality events are raised
        Thread.sleep(3000);

        assertFalse(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        alice.validateUnknownLocalNetworkQualityLevel();

        assertFalse(bob.localParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        bob.validateUnknownLocalNetworkQualityLevel();

        assertFalse(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertFalse(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        bob.validateUnknownRemoteNetworkQualityLevel();
    }

    @Test
    public void shouldObserveUnknownNetworkQualityIfDisabledInGroupRoom()
            throws InterruptedException {
        createRoom(Topology.GROUP_SMALL);
        alice.connectToRoom(roomName, false);
        bob.connectToRoom(roomName, false);
        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Waiting for a short bit to ensure no network quality events are raised
        Thread.sleep(3000);

        assertFalse(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        alice.validateUnknownLocalNetworkQualityLevel();

        assertFalse(bob.localParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        bob.validateUnknownLocalNetworkQualityLevel();

        assertFalse(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertFalse(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        bob.validateUnknownRemoteNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidLocalNetworkQualityWithNoTracks() throws InterruptedException {
        // When participants are not publishing audio or video tracks, they will get a local NQL,
        // but the other participants will not receive a remote NQL for that participant.
        createRoom(Topology.GROUP_SMALL);
        alice.connectToRoom(roomName, true);
        bob.connectToRoom(roomName, true);
        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertTrue(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        alice.validateLocalNetworkQualityLevel();

        assertTrue(
                bob.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateLocalNetworkQualityLevel();

        // Waiting for a short bit to ensure no remote participant network quality events are raised
        Thread.sleep(3000);

        assertFalse(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertFalse(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        bob.validateUnknownRemoteNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidNetworkQualityWithAudioTrack() throws InterruptedException {
        createRoom(Topology.GROUP_SMALL);
        alice.connectToRoom(roomName, true);

        alice.publishAudioTrack();

        assertTrue(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        alice.validateLocalNetworkQualityLevel();

        bob.connectToRoom(roomName, true);
        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertTrue(
                bob.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateLocalNetworkQualityLevel();
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertTrue(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateRemoteNetworkQualityLevel();

        bob.publishAudioTrack();

        assertTrue(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        alice.validateRemoteNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidNetworkQualityWithVideoTrack() throws InterruptedException {
        createRoom(Topology.GROUP_SMALL);
        alice.connectToRoom(roomName, true);

        alice.publishVideoTrack();

        assertTrue(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        alice.validateLocalNetworkQualityLevel();

        bob.connectToRoom(roomName, true);
        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertTrue(
                bob.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateLocalNetworkQualityLevel();
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertTrue(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateRemoteNetworkQualityLevel();

        bob.publishVideoTrack();

        assertTrue(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        alice.validateRemoteNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidLocalNetworkQualityWithDataTrack() throws InterruptedException {
        // When participants are only publishing data tracks, they will get a local NQL, but the
        // other participants will not receive a remote NQL for that participant.
        createRoom(Topology.GROUP_SMALL);
        alice.connectToRoom(roomName, true);

        alice.publishDataTrack();

        assertTrue(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        alice.validateLocalNetworkQualityLevel();

        bob.connectToRoom(roomName, true);
        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertTrue(
                bob.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateLocalNetworkQualityLevel();
        alice.validateUnknownRemoteNetworkQualityLevel();

        bob.publishDataTrack();

        // Waiting for a short bit to ensure no remote participant network quality events are raised
        Thread.sleep(3000);

        assertFalse(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertFalse(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.getCount() == 0);
        bob.validateUnknownRemoteNetworkQualityLevel();
    }

    @Test
    public void shouldObserveValidNetworkQualityWithAudioVideoAndDataTracks()
            throws InterruptedException {
        createRoom(Topology.GROUP_SMALL);
        alice.connectToRoom(roomName, true);

        alice.publishAudioTrack();
        alice.publishVideoTrack();
        alice.publishDataTrack();

        assertTrue(
                alice.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        alice.validateLocalNetworkQualityLevel();

        bob.connectToRoom(roomName, true);
        assertTrue(
                alice.roomListener.onParticipantConnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertTrue(
                bob.localParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateLocalNetworkQualityLevel();
        alice.validateUnknownRemoteNetworkQualityLevel();

        assertTrue(
                bob.remoteParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        bob.validateRemoteNetworkQualityLevel();

        bob.publishAudioTrack();
        bob.publishVideoTrack();
        bob.publishDataTrack();

        assertTrue(
                alice.remoteParticipantListener.onNetworkQualityLevelChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        alice.validateRemoteNetworkQualityLevel();
    }

    class NetworkQualityTestParticipant {
        String identity;
        String token;
        Room room;

        IceOptions iceOptions;
        NetworkQualityConfiguration networkQualityConfiguration;

        LocalParticipant localParticipant;
        LocalAudioTrack localAudioTrack;
        LocalVideoTrack localVideoTrack;
        LocalDataTrack localDataTrack;

        RemoteParticipant remoteParticipant;

        // Listeners
        RoomListener roomListener;
        CallbackHelper.FakeLocalParticipantListener localParticipantListener;
        CallbackHelper.FakeRemoteParticipantListener remoteParticipantListener;

        public NetworkQualityTestParticipant(@NonNull String identity) {
            this.identity = identity;
            token = CredentialsUtils.getAccessToken(identity);

            iceOptions =
                    new IceOptions.Builder()
                            .abortOnIceServersTimeout(true)
                            .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                            .build();

            roomListener = new RoomListener();
            roomListener.onConnectedLatch = new CountDownLatch(1);
            roomListener.onParticipantConnectedLatch = new CountDownLatch(1);

            localParticipantListener = new CallbackHelper.FakeLocalParticipantListener();
            localParticipantListener.onNetworkQualityLevelChangedLatch = new CountDownLatch(1);

            remoteParticipantListener = new CallbackHelper.FakeRemoteParticipantListener();
            remoteParticipantListener.onNetworkQualityLevelChangedLatch = new CountDownLatch(1);
        }

        public void connectToRoom(String roomName, Boolean isNetworkQualityEnabled)
                throws InterruptedException {
            NetworkQualityConfiguration networkQualityConfiguration =
                    new NetworkQualityConfiguration(
                            NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                            NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);

            ConnectOptions connectOptions =
                    new ConnectOptions.Builder(token)
                            .roomName(roomName)
                            .enableNetworkQuality(isNetworkQualityEnabled)
                            .networkQualityConfiguration(networkQualityConfiguration)
                            .iceOptions(iceOptions)
                            .build();

            room = Video.connect(mediaTestActivity, connectOptions, roomListener);
            assertTrue(
                    roomListener.onConnectedLatch.await(
                            TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        }

        public void disconnectRoom() throws InterruptedException {
            if (room != null && room.getState() != Room.State.DISCONNECTED) {
                roomListener.onDisconnectedLatch = new CountDownLatch(1);
                room.disconnect();
                assertTrue(
                        roomListener.onDisconnectedLatch.await(
                                TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
            }
        }

        public void publishAudioTrack() throws InterruptedException {
            localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
            localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
            assertNotNull(localAudioTrack);
            assertTrue(localParticipant.publishTrack(alice.localAudioTrack));

            assertTrue(
                    localParticipantListener.onPublishedAudioTrackLatch.await(
                            TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        }

        public void publishVideoTrack() throws InterruptedException {
            localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
            FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
            localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
            assertNotNull(localVideoTrack);
            assertTrue(localParticipant.publishTrack(localVideoTrack));

            assertTrue(
                    localParticipantListener.onPublishedVideoTrackLatch.await(
                            TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        }

        public void publishDataTrack() throws InterruptedException {
            localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
            localDataTrack = LocalDataTrack.create(mediaTestActivity);
            assertNotNull(localDataTrack);
            assertTrue(localParticipant.publishTrack(localDataTrack));
            assertTrue(
                    localParticipantListener.onPublishedDataTrackLatch.await(
                            TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        }

        public void validateUnknownLocalNetworkQualityLevel() {
            validateUnknownNetworkQualityLevel(localParticipant);
        }

        public void validateUnknownRemoteNetworkQualityLevel() {
            validateUnknownNetworkQualityLevel(remoteParticipant);
        }

        private void validateUnknownNetworkQualityLevel(Participant participant) {
            NetworkQualityLevel networkQualityLevel = participant.getNetworkQualityLevel();
            assertNotNull(networkQualityLevel);
            assertEquals(NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, networkQualityLevel);
        }

        public void validateLocalNetworkQualityLevel() {
            validateNetworkQualityLevel(
                    localParticipant, localParticipantListener.onNetworkLevelChangedEvents);
        }

        public void validateRemoteNetworkQualityLevel() {
            validateNetworkQualityLevel(
                    remoteParticipant, remoteParticipantListener.onNetworkLevelChangedEvents);
        }

        private void validateNetworkQualityLevel(
                Participant participant, List<NetworkQualityLevel> networkQualityLevelEvents) {
            assertNotNull(participant);
            NetworkQualityLevel networkQualityLevel = participant.getNetworkQualityLevel();
            assertNotNull(networkQualityLevel);
            assertNotEquals(NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, networkQualityLevel);
            NetworkQualityLevel callbackNetworkQualityLevel =
                    networkQualityLevelEvents.get(networkQualityLevelEvents.size() - 1);
            assertNotNull(callbackNetworkQualityLevel);
            assertNotEquals(
                    NetworkQualityLevel.NETWORK_QUALITY_LEVEL_UNKNOWN, callbackNetworkQualityLevel);
        }

        public void cleanup() {
            if (localAudioTrack != null) {
                localAudioTrack.release();
                localAudioTrack = null;
            }
            if (localVideoTrack != null) {
                localVideoTrack.release();
                localVideoTrack = null;
            }
            if (localDataTrack != null) {
                localDataTrack.release();
                localDataTrack = null;
            }
        }

        class RoomListener extends CallbackHelper.FakeRoomListener {

            @Override
            public void onConnected(@NonNull Room room) {
                super.onConnected(room);

                NetworkQualityTestParticipant.this.localParticipant = room.getLocalParticipant();
                assertNotNull(NetworkQualityTestParticipant.this.localParticipant);
                NetworkQualityTestParticipant.this.localParticipant.setListener(
                        NetworkQualityTestParticipant.this.localParticipantListener);

                if (!room.getRemoteParticipants().isEmpty()) {
                    NetworkQualityTestParticipant.this.remoteParticipant =
                            room.getRemoteParticipants().get(0);
                    NetworkQualityTestParticipant.this.remoteParticipant.setListener(
                            NetworkQualityTestParticipant.this.remoteParticipantListener);
                }
            }

            @Override
            public void onParticipantConnected(
                    @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
                super.onParticipantConnected(room, remoteParticipant);
                NetworkQualityTestParticipant.this.remoteParticipant = remoteParticipant;
                NetworkQualityTestParticipant.this.remoteParticipant.setListener(
                        NetworkQualityTestParticipant.this.remoteParticipantListener);
            }
        }
    }
}
