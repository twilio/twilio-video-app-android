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
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import com.kevinmost.junit_retry_rule.Retry;
import com.kevinmost.junit_retry_rule.RetryRule;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.ArrayList;
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

@RunWith(Parameterized.class)
@LargeTest
public class RemoteParticipantTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {{Topology.P2P}, {Topology.GROUP}, {Topology.GROUP_SMALL}});
    }

    @Rule public final RetryRule retryRule = new RetryRule();
    private Context context;
    private String tokenOne;
    private String tokenTwo;
    private String roomName;
    private Room room;
    private final CallbackHelper.FakeRoomListener roomListener =
            new CallbackHelper.FakeRoomListener();
    private Room otherRoom;
    private final CallbackHelper.FakeRoomListener otherRoomListener =
            new CallbackHelper.FakeRoomListener();
    private final Topology topology;

    public RemoteParticipantTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.baseSetup(topology);
        roomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        tokenOne = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        tokenTwo = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        disconnect(room, roomListener);
        disconnect(otherRoom, otherRoomListener);
        /*
         * After all participants have disconnected complete the room to clean up backend
         * resources.
         */
        Room roomToComplete = room != null ? room : otherRoom;
        if (roomToComplete != null) {
            RoomUtils.completeRoom(roomToComplete);
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void participantCanConnect() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(tokenOne)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        connectOptions =
                new ConnectOptions.Builder(tokenTwo)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        otherRoomListener.onDisconnectedLatch = new CountDownLatch(1);
        otherRoom = Video.connect(context, connectOptions, otherRoomListener);
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getRemoteParticipants().size());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void participantCanDisconnect() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(tokenOne)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        ConnectOptions connectOptions2 =
                new ConnectOptions.Builder(tokenTwo)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        otherRoomListener.onConnectedLatch = new CountDownLatch(1);
        otherRoomListener.onDisconnectedLatch = new CountDownLatch(1);
        otherRoom = Video.connect(context, connectOptions2, otherRoomListener);

        assertTrue(otherRoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        List<RemoteParticipant> client2RemoteParticipants =
                new ArrayList<>(otherRoom.getRemoteParticipants());
        RemoteParticipant client1RemoteParticipant = client2RemoteParticipants.get(0);

        assertEquals(1, client2RemoteParticipants.size());
        assertTrue(client1RemoteParticipant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        List<RemoteParticipant> client1RemoteParticipants =
                new ArrayList<>(room.getRemoteParticipants());
        RemoteParticipant client2RemoteParticipant = client1RemoteParticipants.get(0);

        assertEquals(1, client1RemoteParticipants.size());
        assertTrue(client2RemoteParticipant.isConnected());

        otherRoom.disconnect();
        assertTrue(otherRoomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client2RemoteParticipant.isConnected());
        assertTrue(room.getRemoteParticipants().isEmpty());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client1RemoteParticipant.isConnected());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void participantShouldHaveValidIdentity() throws InterruptedException {
        String expectedIdentity = random(50);
        tokenOne = CredentialsUtils.getAccessToken(expectedIdentity, topology);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(tokenOne)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        ConnectOptions connectOptions2 =
                new ConnectOptions.Builder(tokenTwo)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        otherRoomListener.onConnectedLatch = new CountDownLatch(1);
        otherRoomListener.onDisconnectedLatch = new CountDownLatch(1);
        otherRoom = Video.connect(context, connectOptions2, otherRoomListener);

        assertTrue(otherRoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        List<RemoteParticipant> client2RemoteParticipants =
                new ArrayList<>(otherRoom.getRemoteParticipants());
        RemoteParticipant client1RemoteParticipant = client2RemoteParticipants.get(0);

        assertEquals(1, client2RemoteParticipants.size());
        assertTrue(client1RemoteParticipant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(expectedIdentity, client1RemoteParticipant.getIdentity());

        List<RemoteParticipant> client1RemoteParticipants =
                new ArrayList<>(room.getRemoteParticipants());
        RemoteParticipant client2RemoteParticipant = client1RemoteParticipants.get(0);

        assertEquals(1, client1RemoteParticipants.size());
        assertTrue(client2RemoteParticipant.isConnected());

        otherRoom.disconnect();
        assertTrue(otherRoomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client2RemoteParticipant.isConnected());
        assertTrue(room.getRemoteParticipants().isEmpty());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client1RemoteParticipant.isConnected());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void shouldReceiveTrackEvents() throws InterruptedException {
        // Audio track added and subscribed
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackPublishedLatch = new CountDownLatch(1);
        participantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        bobRemoteParticipant.setListener(participantListener);
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(bobRoom.getLocalParticipant().publishTrack(bobLocalAudioTrack));
        assertTrue(participantListener.onAudioTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToAudioTrackLatch.await(20, TimeUnit.SECONDS));

        // Audio track disabled
        participantListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        bobLocalAudioTrack.enable(false);
        assertTrue(participantListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track enabled
        participantListener.onAudioTrackEnabledLatch = new CountDownLatch(1);
        bobLocalAudioTrack.enable(true);
        assertTrue(participantListener.onAudioTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track removed and unsubscribed
        participantListener.onAudioTrackUnpublishedLatch = new CountDownLatch(1);
        participantListener.onUnsubscribedFromAudioTrackLatch = new CountDownLatch(1);
        bobRoom.getLocalParticipant().unpublishTrack(bobLocalAudioTrack);
        assertTrue(
                participantListener.onUnsubscribedFromAudioTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onAudioTrackUnpublishedLatch.await(20, TimeUnit.SECONDS));

        // Video track added and subscribed
        participantListener.onVideoTrackPublishedLatch = new CountDownLatch(1);
        participantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        bobLocalVideoTrack =
                LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        assertTrue(bobRoom.getLocalParticipant().publishTrack(bobLocalVideoTrack));
        assertTrue(participantListener.onVideoTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));

        // Video track disabled
        participantListener.onVideoTrackDisabledLatch = new CountDownLatch(1);
        bobLocalVideoTrack.enable(false);
        assertTrue(participantListener.onVideoTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Video track enabled
        participantListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        bobLocalVideoTrack.enable(true);
        assertTrue(participantListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Video track removed and unsubscribed
        participantListener.onVideoTrackUnpublishedLatch = new CountDownLatch(1);
        participantListener.onUnsubscribedFromVideoTrackLatch = new CountDownLatch(1);
        bobRoom.getLocalParticipant().unpublishTrack(bobLocalVideoTrack);
        assertTrue(
                participantListener.onUnsubscribedFromVideoTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onVideoTrackUnpublishedLatch.await(20, TimeUnit.SECONDS));

        // Data track published and subscribed
        participantListener.onDataTrackPublishedLatch = new CountDownLatch(1);
        participantListener.onSubscribedToDataTrackLatch = new CountDownLatch(1);
        bobLocalDataTrack = LocalDataTrack.create(mediaTestActivity);
        assertTrue(bobRoom.getLocalParticipant().publishTrack(bobLocalDataTrack));
        assertTrue(participantListener.onDataTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onSubscribedToDataTrackLatch.await(20, TimeUnit.SECONDS));

        // Data track unsubscribed and unpublished
        participantListener.onDataTrackUnpublishedLatch = new CountDownLatch(1);
        participantListener.onUnsubscribedFromDataTrackLatch = new CountDownLatch(1);
        bobRoom.getLocalParticipant().unpublishTrack(bobLocalDataTrack);
        assertTrue(
                participantListener.onUnsubscribedFromDataTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(participantListener.onDataTrackUnpublishedLatch.await(20, TimeUnit.SECONDS));

        // Validate the order of events
        List<String> expectedParticipantEvents =
                Arrays.asList(
                        "onAudioTrackPublished",
                        "onAudioTrackSubscribed",
                        "onAudioTrackDisabled",
                        "onAudioTrackEnabled",
                        "onAudioTrackUnsubscribed",
                        "onAudioTrackUnpublished",
                        "onVideoTrackPublished",
                        "onVideoTrackSubscribed",
                        "onVideoTrackDisabled",
                        "onVideoTrackEnabled",
                        "onVideoTrackUnsubscribed",
                        "onVideoTrackUnpublished",
                        "onDataTrackPublished",
                        "onDataTrackSubscribed",
                        "onDataTrackUnsubscribed",
                        "onDataTrackUnpublished");
        assertArrayEquals(
                expectedParticipantEvents.toArray(),
                participantListener.participantEvents.toArray());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void shouldHaveTracksAfterDisconnected() throws InterruptedException {
        // Add audio and video tracks
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        this.bobRemoteParticipant.setListener(participantListener);
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, false);
        assertTrue(bobRoom.getLocalParticipant().publishTrack(bobLocalAudioTrack));
        assertTrue(participantListener.onSubscribedToAudioTrackLatch.await(20, TimeUnit.SECONDS));
        participantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        bobLocalVideoTrack =
                LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        assertTrue(bobRoom.getLocalParticipant().publishTrack(bobLocalVideoTrack));
        assertTrue(participantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));

        // Cache bobRemoteParticipant two tracks
        List<RemoteAudioTrackPublication> remoteAudioTrackPublications =
                aliceRoomListener.getRemoteParticipant().getRemoteAudioTracks();
        List<AudioTrackPublication> audioTrackPublications =
                aliceRoomListener.getRemoteParticipant().getAudioTracks();
        List<RemoteVideoTrackPublication> remoteVideoTrackPublications =
                aliceRoomListener.getRemoteParticipant().getRemoteVideoTracks();
        List<VideoTrackPublication> videoTrackPublications =
                aliceRoomListener.getRemoteParticipant().getVideoTracks();

        // RemoteParticipant two disconnects
        aliceRoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        bobRoom.disconnect();
        assertTrue(aliceRoomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        RemoteParticipant remoteParticipant = aliceRoomListener.getRemoteParticipant();

        // Validate enabled matches last known state
        assertEquals(
                remoteAudioTrackPublications.get(0).isTrackEnabled(),
                remoteParticipant.getRemoteAudioTracks().get(0).isTrackEnabled());
        assertEquals(
                audioTrackPublications.get(0).isTrackEnabled(),
                remoteParticipant.getAudioTracks().get(0).isTrackEnabled());
        assertEquals(
                remoteVideoTrackPublications.get(0).isTrackEnabled(),
                remoteParticipant.getRemoteVideoTracks().get(0).isTrackEnabled());
        assertEquals(
                videoTrackPublications.get(0).isTrackEnabled(),
                remoteParticipant.getVideoTracks().get(0).isTrackEnabled());

        // Validate alice is no longer subscribed to bob tracks
        assertFalse(remoteParticipant.getRemoteAudioTracks().get(0).isTrackSubscribed());
        assertEquals(
                remoteAudioTrackPublications.get(0).isTrackSubscribed(),
                remoteParticipant.getRemoteAudioTracks().get(0).isTrackSubscribed());
        assertFalse(remoteParticipant.getRemoteVideoTracks().get(0).isTrackSubscribed());
        assertEquals(
                remoteVideoTrackPublications.get(0).isTrackSubscribed(),
                remoteParticipant.getRemoteVideoTracks().get(0).isTrackSubscribed());

        // Validate the track objects are equal
        assertEquals(
                remoteAudioTrackPublications.get(0),
                remoteParticipant.getRemoteAudioTracks().get(0));
        assertEquals(audioTrackPublications.get(0), remoteParticipant.getAudioTracks().get(0));
        assertEquals(
                remoteVideoTrackPublications.get(0),
                remoteParticipant.getRemoteVideoTracks().get(0));
        assertEquals(videoTrackPublications.get(0), remoteParticipant.getVideoTracks().get(0));
    }
}
