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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class RemoteParticipantTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    private Context context;
    private String tokenOne;
    private String tokenTwo;
    private String roomName;
    private final Topology topology;

    public RemoteParticipantTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.baseSetup(topology);
        roomName = RandUtils.generateRandomString(20);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        tokenOne = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        tokenTwo = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void participantCanConnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(tokenOne)
                .roomName(roomName)
                .build();
        Room room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        connectOptions = new ConnectOptions.Builder(tokenTwo)
            .roomName(roomName)
            .build();
        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        roomListener2.onDisconnectedLatch = new CountDownLatch(1);
        Room room2 = Video.connect(context, connectOptions, roomListener2);
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getRemoteParticipants().size());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());
        room2.disconnect();
        assertTrue(roomListener2.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void participantCanDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(tokenOne)
                .roomName(roomName)
                .build();
        Room room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        ConnectOptions connectOptions2 = new ConnectOptions.Builder(tokenTwo)
            .roomName(roomName)
            .build();
        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        roomListener2.onConnectedLatch = new CountDownLatch(1);
        roomListener2.onDisconnectedLatch = new CountDownLatch(1);
        Room client2room = Video.connect(context, connectOptions2, roomListener2);

        assertTrue(roomListener2.onConnectedLatch.await(20, TimeUnit.SECONDS));

        List<RemoteParticipant> client2RemoteParticipants = new ArrayList<>(client2room.getRemoteParticipants());
        RemoteParticipant client1RemoteParticipant = client2RemoteParticipants.get(0);

        assertEquals(1, client2RemoteParticipants.size());
        assertTrue(client1RemoteParticipant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        List<RemoteParticipant> client1RemoteParticipants = new ArrayList<>(room.getRemoteParticipants());
        RemoteParticipant client2RemoteParticipant = client1RemoteParticipants.get(0);

        assertEquals(1, client1RemoteParticipants.size());
        assertTrue(client2RemoteParticipant.isConnected());

        client2room.disconnect();
        assertTrue(roomListener2.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client2RemoteParticipant.isConnected());
        assertTrue(room.getRemoteParticipants().isEmpty());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client1RemoteParticipant.isConnected());
    }

    @Test
    public void shouldReceiveTrackEvents() throws InterruptedException {
        // Audio track added
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        remoteParticipant.setListener(participantListener);
        bobPublishableLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(bobRoom.getLocalParticipant().publishAudioTrack(bobPublishableLocalAudioTrack));
        assertTrue(participantListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Audio track disabled
        participantListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        bobPublishableLocalAudioTrack.enable(false);
        assertTrue(participantListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track enabled
        participantListener.onAudioTrackEnabledLatch = new CountDownLatch(1);
        bobPublishableLocalAudioTrack.enable(true);
        assertTrue(participantListener.onAudioTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track removed
        participantListener.onAudioTrackRemovedLatch = new CountDownLatch(1);
        bobRoom.getLocalParticipant().unpublishAudioTrack(bobPublishableLocalAudioTrack);
        assertTrue(participantListener.onAudioTrackRemovedLatch.await(20, TimeUnit.SECONDS));

        // Video track added
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        bobPublishableLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        assertTrue(bobRoom.getLocalParticipant().publishVideoTrack(bobPublishableLocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Video track disabled
        participantListener.onVideoTrackDisabledLatch = new CountDownLatch(1);
        bobPublishableLocalVideoTrack.enable(false);
        assertTrue(participantListener.onVideoTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Video track enabled
        participantListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        bobPublishableLocalVideoTrack.enable(true);
        assertTrue(participantListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Video track removed
        participantListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        bobRoom.getLocalParticipant().unpublishVideoTrack(bobPublishableLocalVideoTrack);
        assertTrue(participantListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    @Ignore("Will not work until we probably implement the track subscription APIs GSDK-1214")
    public void shouldHaveTracksAfterDisconnected() throws InterruptedException {
        // Add audio and video tracks
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        this.remoteParticipant.setListener(participantListener);
        bobPublishableLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, false);
        assertTrue(bobRoom.getLocalParticipant().publishAudioTrack(bobPublishableLocalAudioTrack));
        assertTrue(participantListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        bobPublishableLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        assertTrue(bobRoom.getLocalParticipant().publishVideoTrack(bobPublishableLocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Cache remoteParticipant two tracks
        List<RemoteAudioTrack> remoteAudioTracks = aliceRoomListener
                .getRemoteParticipant()
                .getRemoteAudioTracks();
        List<AudioTrack> audioTracks = aliceRoomListener
                .getRemoteParticipant()
                .getAudioTracks();
        List<RemoteVideoTrack> remoteVideoTracks = aliceRoomListener
                .getRemoteParticipant()
                .getRemoteVideoTracks();
        List<VideoTrack> videoTracks = aliceRoomListener
                .getRemoteParticipant()
                .getVideoTracks();

        // RemoteParticipant two disconnects
        aliceRoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        bobRoom.disconnect();
        assertTrue(aliceRoomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        RemoteParticipant remoteParticipant = aliceRoomListener.getRemoteParticipant();

        // Validate that disconnected remoteParticipant has all tracks
        assertEquals(remoteAudioTracks.get(0).isEnabled(),
                remoteParticipant.getRemoteAudioTracks().get(0).isEnabled());
        assertEquals(audioTracks.get(0).isEnabled(),
                remoteParticipant.getAudioTracks().get(0).isEnabled());
        assertEquals(remoteAudioTracks.get(0), remoteParticipant.getRemoteAudioTracks().get(0));
        assertEquals(audioTracks.get(0), remoteParticipant.getAudioTracks().get(0));
        assertEquals(remoteVideoTracks.get(0).isEnabled(),
                remoteParticipant.getRemoteVideoTracks().get(0).isEnabled());
        assertEquals(videoTracks.get(0).isEnabled(),
                remoteParticipant.getVideoTracks().get(0).isEnabled());
        assertEquals(remoteVideoTracks.get(0), remoteParticipant.getRemoteVideoTracks().get(0));
        assertEquals(videoTracks.get(0), remoteParticipant.getVideoTracks().get(0));
    }
}
