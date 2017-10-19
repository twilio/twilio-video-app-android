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

import android.Manifest;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@LargeTest
public class LocalParticipantTopologyTest extends BaseClientTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    @Rule
    public GrantPermissionRule recordAudioPermissionRule = GrantPermissionRule
            .grant(Manifest.permission.RECORD_AUDIO);
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
    private LocalDataTrack localDataTrack;
    private Topology topology;

    public LocalParticipantTopologyTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        identity = Constants.PARTICIPANT_ALICE;
        roomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        token = CredentialsUtils.getAccessToken(identity, topology);
    }

    @After
    public void teardown() throws InterruptedException {
        if (room != null && room.getState() != RoomState.DISCONNECTED) {
            roomListener.onDisconnectedLatch = new CountDownLatch(1);
            room.disconnect();
            assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
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
    public void shouldAllowPublishingAndUnpublishingTracksWhileConnected()
            throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);

        // Now publish tracks
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        assertTrue(localParticipant.publishTrack(localAudioTrack));
        assertEquals(0, localParticipant.getAudioTracks().size());
        assertEquals(0, localParticipant.getLocalAudioTracks().size());
        assertTrue(localParticipant.publishTrack(localVideoTrack));
        assertEquals(0, localParticipant.getVideoTracks().size());
        assertEquals(0, localParticipant.getLocalVideoTracks().size());
        // TODO: Enable for group rooms GSDK-1324
        if (topology == Topology.P2P) {
            assertTrue(localParticipant.publishTrack(localDataTrack));
            assertEquals(0, localParticipant.getDataTracks().size());
            assertEquals(0, localParticipant.getLocalDataTracks().size());
        }

        // Validate we received callbacks
        assertTrue(localParticipantListener.onPublishedAudioTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(localParticipantListener.onPublishedVideoTrackLatch.await(20, TimeUnit.SECONDS));
        if (topology == Topology.P2P) {
            assertTrue(localParticipantListener.onPublishedDataTrackLatch.await(20,
                    TimeUnit.SECONDS));
        }

        // Now unpublish tracks
        assertTrue(localParticipant.unpublishTrack(localAudioTrack));
        assertEquals(0, localParticipant.getAudioTracks().size());
        assertEquals(0, localParticipant.getLocalAudioTracks().size());
        assertTrue(localParticipant.unpublishTrack(localVideoTrack));
        assertEquals(0, localParticipant.getVideoTracks().size());
        assertEquals(0, localParticipant.getLocalVideoTracks().size());
        // TODO: Enable for group rooms GSDK-1324
        if (topology == Topology.P2P) {
            assertTrue(localParticipant.unpublishTrack(localDataTrack));
            assertEquals(0, localParticipant.getDataTracks().size());
            assertEquals(0, localParticipant.getLocalDataTracks().size());
        }
    }

    @Test
    public void shouldHaveTracksWhenConnected() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);

        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(token)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .roomName(roomName);

        // TODO: Enable for group rooms GSDK-1324
        if (topology == Topology.P2P) {
            connectOptionsBuilder.dataTracks(Collections.singletonList(localDataTrack));
        }

        room = Video.connect(mediaTestActivity, connectOptionsBuilder.build(), roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);

        // Validate the state of tracks
        assertEquals(1, localParticipant.getAudioTracks().size());
        assertEquals(1, localParticipant.getLocalAudioTracks().size());
        assertEquals(1, localParticipant.getVideoTracks().size());
        assertEquals(1, localParticipant.getLocalVideoTracks().size());
        if (topology == Topology.P2P) {
            assertEquals(1, localParticipant.getDataTracks().size());
            assertEquals(1, localParticipant.getLocalDataTracks().size());
        }
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0).getAudioTrack());
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0).getVideoTrack());
        if (topology == Topology.P2P) {
            assertEquals(localDataTrack, localParticipant.getDataTracks().get(0).getDataTrack());
        }
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidOnceConnected() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidUponDisconnect() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        room.disconnect();
        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidOnceDisconnected() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
    }

    @Test
    public void shouldNotPublishAudioTrackAfterDisconnect() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        int audioTrackSize = localParticipant.getLocalAudioTracks().size();
        room.disconnect();
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertFalse(localParticipant.publishTrack(localAudioTrack));
        assertEquals(audioTrackSize, localParticipant.getAudioTracks().size());
        assertEquals(audioTrackSize, localParticipant.getLocalAudioTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotPublishVideoTrackAfterDisconnect() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        int videoTrackSize = localParticipant.getLocalVideoTracks().size();
        room.disconnect();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        assertFalse(localParticipant.publishTrack(localVideoTrack));
        assertEquals(videoTrackSize, localParticipant.getVideoTracks().size());
        assertEquals(videoTrackSize, localParticipant.getLocalVideoTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotPublishDataTrackAfterDisconnect() throws InterruptedException {
        // TODO: Enable for group rooms GSDK-1324
        assumeTrue(topology == Topology.P2P);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        int dataTrackSize = localParticipant.getLocalDataTracks().size();
        room.disconnect();
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        assertFalse(localParticipant.publishTrack(localDataTrack));
        assertEquals(dataTrackSize, localParticipant.getDataTracks().size());
        assertEquals(dataTrackSize, localParticipant.getLocalDataTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotUnpublishAudioTrackAfterDisconnect() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localParticipant.publishTrack(localAudioTrack);
        assertTrue(localParticipantListener.onPublishedAudioTrackLatch.await(20, TimeUnit.SECONDS));
        int audioTrackSize = localParticipant.getLocalAudioTracks().size();
        room.disconnect();
        assertFalse(localParticipant.unpublishTrack(localAudioTrack));
        assertEquals(audioTrackSize, localParticipant.getAudioTracks().size());
        assertEquals(audioTrackSize, localParticipant.getLocalAudioTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotUnpublishVideoTrackAfterDisconnect() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localParticipant.publishTrack(localVideoTrack);
        assertTrue(localParticipantListener.onPublishedVideoTrackLatch.await(20, TimeUnit.SECONDS));
        int videoTrackSize = localParticipant.getLocalVideoTracks().size();
        room.disconnect();
        assertFalse(localParticipant.unpublishTrack(localVideoTrack));
        assertEquals(videoTrackSize, localParticipant.getVideoTracks().size());
        assertEquals(videoTrackSize, localParticipant.getLocalVideoTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotUnpublishDataTrackAfterDisconnect() throws InterruptedException {
        // TODO: Enable for group rooms GSDK-1324
        assumeTrue(topology == Topology.P2P);
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        localParticipant.publishTrack(localDataTrack);
        assertTrue(localParticipantListener.onPublishedDataTrackLatch.await(20, TimeUnit.SECONDS));
        int dataTrackSize = localParticipant.getLocalDataTracks().size();
        room.disconnect();
        assertFalse(localParticipant.unpublishTrack(localDataTrack));
        assertEquals(dataTrackSize, localParticipant.getDataTracks().size());
        assertEquals(dataTrackSize, localParticipant.getLocalDataTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    /*
     * TODO: Add better validation of EncodingParameters scenarios.
     *
     * Currently these scenarios are just executed, and there is no way to validate the operation.
     */

    @Test
    public void shouldNotSetEncodingParametersAfterDisconnect() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localParticipant.publishTrack(localVideoTrack);
        assertTrue(localParticipantListener.onPublishedVideoTrackLatch.await(20, TimeUnit.SECONDS));
        room.disconnect();
        EncodingParameters encodingParameters = new EncodingParameters(64000, 800000);
        localParticipant.setEncodingParameters(encodingParameters);
    }

    @Test
    public void shouldAllowNullEncodingParameters() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        EncodingParameters encodingParameters = new EncodingParameters(64000, 800000);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .roomName(roomName)
                .encodingParameters(encodingParameters)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        localParticipant.setEncodingParameters(null);
    }

    @Test
    public void shouldAllowEncodingParameters() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        EncodingParameters encodingParameters = new EncodingParameters(64000, 800000);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedAudioTrackLatch = new CountDownLatch(1);
        localParticipantListener.onPublishedVideoTrackLatch = new CountDownLatch(1);

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .roomName(roomName)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localParticipant.setListener(localParticipantListener);
        localParticipant.setEncodingParameters(encodingParameters);
    }
}
