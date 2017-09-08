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
import static org.junit.Assume.assumeTrue;

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
    private Room room;

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
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void connect_shouldConnectToRoom() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getSid(), room.getName());
    }

    @Test
    public void canConnectWithInsightsDisabled() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .enableInsights(false)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getSid(), room.getName());
    }

    @Test
    @Ignore("Disconnecting while connecting results in native crash. See GSDK-1153")
    public void disconnect_canDisconnectBeforeConnectingToRoom() throws InterruptedException {
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
    }

    @Test
    public void connect_shouldAllowAudioTracks() throws InterruptedException {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        List<LocalAudioTrack> localAudioTrackList =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTrackList)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        // Validate tracks in local participant
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getAudioTracks().get(0));
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0).getAudioTrack());
        assertTrue(localParticipant.unpublishTrack(localAudioTrack));
    }

    @Test
    public void connect_shouldAllowVideoTracks() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        List<LocalVideoTrack> localVideoTrackList =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTrackList)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getLocalVideoTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0).getVideoTrack());
        assertTrue(localParticipant.unpublishTrack(localVideoTrack));
    }

    @Test
    public void connect_shouldAllowAudioAndVideoTracks() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        List<LocalAudioTrack> localAudioTrackList =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};
        List<LocalVideoTrack> localVideoTrackList =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTrackList)
                .videoTracks(localVideoTrackList)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getLocalAudioTracks().get(0));
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0).getAudioTrack());
        assertNotNull(localParticipant.getLocalVideoTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0).getVideoTrack());
        assertTrue(localParticipant.unpublishTrack(localAudioTrack));
        assertTrue(localParticipant.unpublishTrack(localVideoTrack));
    }

    @Test
    public void connect_shouldFailToConnectWithBadToken() throws InterruptedException {
        roomListener.onConnectFailureLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder("bad token")
            .roomName(roomName)
            .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectFailureLatch.await(20, TimeUnit.SECONDS));
        assertEquals(roomListener.getTwilioException().getCode(),
            TwilioException.ACCESS_TOKEN_INVALID_EXCEPTION);
        assertNotNull(roomListener.getTwilioException().getMessage());
    }

    @Test
    public void connect_shouldAllowLocalVideoTrackToBeReleasedWhileConnecting()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTracks)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);

        // Add sleep to ensure that connect has started
        Thread.sleep(200);

        localVideoTrack.release();
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(localVideoTrack, room.getLocalParticipant().getLocalVideoTracks().get(0).getLocalVideoTrack());
        assertTrue(room.getLocalParticipant().getLocalVideoTracks().get(0).getLocalVideoTrack().isReleased());
    }

    @Test
    public void connect_shouldAllowLocalAudioTrackToBeReleasedWhileConnecting()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTracks)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);

        // Add sleep to ensure that connect has started
        Thread.sleep(200);

        localAudioTrack.release();
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(localAudioTrack, room.getLocalParticipant().getLocalAudioTracks().get(0).getLocalAudioTrack());
        assertTrue(room.getLocalParticipant().getLocalAudioTracks().get(0).getLocalAudioTrack().isReleased());
    }

    @Test
    public void connect_shouldAllowLocalVideoTrackToBeReleasedAfterConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTracks)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        localVideoTrack.release();
    }

    @Test
    public void connect_shouldAllowLocalAudioTrackToBeReleasedAfterConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTracks)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        localAudioTrack.release();
    }

    @Test
    public void connect_shouldAllowEncodingParameters() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        List<LocalAudioTrack> localAudioTrackList =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};
        List<LocalVideoTrack> localVideoTrackList =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};
        EncodingParameters encodingParameters = new EncodingParameters(64000, 800000);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTrackList)
                .videoTracks(localVideoTrackList)
                .encodingParameters(encodingParameters)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant.getLocalAudioTracks().get(0));
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0).getAudioTrack());
        assertNotNull(localParticipant.getLocalVideoTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0).getVideoTrack());
        assertTrue(localParticipant.unpublishTrack(localAudioTrack));
        assertTrue(localParticipant.unpublishTrack(localVideoTrack));
    }
}
