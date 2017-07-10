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

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String identity;
    private String token;
    private String roomName;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private Topology topology;

    public LocalParticipantTopologyTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        identity = Constants.PARTICIPANT_ALICE;
        roomName = RandUtils.generateRandomString(20);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        token = CredentialsUtils.getAccessToken(identity, topology);
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
    public void shouldAllowAddingAndRemovingTracksWhileConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        // Now we add our tracks
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        assertTrue(localParticipant.addAudioTrack(localAudioTrack));
        assertTrue(localParticipant.addVideoTrack(localVideoTrack));

        // Now remove them
        assertTrue(localParticipant.removeAudioTrack(localAudioTrack));
        assertTrue(localParticipant.removeVideoTrack(localVideoTrack));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldHaveTracksWhenConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        assertEquals(1, localParticipant.getAudioTracks().size());
        assertEquals(1, localParticipant.getVideoTracks().size());

        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0));

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidOnceConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidUponDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
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
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
    }

    @Test
    public void shouldNotAddAudioTrackAfterDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        int audio_track_size = localParticipant.getAudioTracks().size();
        room.disconnect();
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertFalse(localParticipant.addAudioTrack(localAudioTrack));
        assertEquals(audio_track_size, localParticipant.getAudioTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotAddVideoTrackAfterDisconnect() throws InterruptedException {
                CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        int video_track_size = localParticipant.getVideoTracks().size();
        room.disconnect();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        assertFalse(localParticipant.addVideoTrack(localVideoTrack));
        assertEquals(video_track_size, localParticipant.getVideoTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotRemoveAudioTrackAfterDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localParticipant.addAudioTrack(localAudioTrack);
        int audio_track_size = localParticipant.getAudioTracks().size();
        room.disconnect();
        assertFalse(localParticipant.removeAudioTrack(localAudioTrack));
        assertEquals(audio_track_size, localParticipant.getAudioTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldNotRemoveVideoTrackAfterDisconnect() throws InterruptedException {
                CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localParticipant.addVideoTrack(localVideoTrack);
        int video_track_size = localParticipant.getVideoTracks().size();
        room.disconnect();
        assertFalse(localParticipant.removeVideoTrack(localVideoTrack));
        assertEquals(video_track_size, localParticipant.getVideoTracks().size());
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

}
