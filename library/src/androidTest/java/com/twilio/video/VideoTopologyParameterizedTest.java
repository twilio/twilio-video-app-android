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

import com.kevinmost.junit_retry_rule.Retry;
import com.kevinmost.junit_retry_rule.RetryRule;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
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

import static junit.framework.TestCase.assertNotNull;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class VideoTopologyParameterizedTest extends BaseVideoTest {
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

    @Rule
    public final RetryRule retryRule = new RetryRule();

    private MediaTestActivity mediaTestActivity;
    private String token;
    private String roomName;
    private Room room;
    private CallbackHelper.FakeRoomListener roomListener;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private LocalDataTrack localDataTrack;
    private final Topology topology;

    public VideoTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomListener = new CallbackHelper.FakeRoomListener();
        roomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        token = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void connect_shouldConnectToRoom() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getSid(), room.getName());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void disconnect_canDisconnectBeforeConnectingToRoom() throws InterruptedException {
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void connect_shouldAllowDataTracks() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        List<LocalDataTrack> localDataTrackList =
                new ArrayList<LocalDataTrack>(){{ add(localDataTrack); }};

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .dataTracks(localDataTrackList)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        localParticipant.setListener(localParticipantListener);

        if (topology == Topology.GROUP) {
            assertTrue(localParticipantListener.onPublishedDataTrackLatch.await(20,
                    TimeUnit.SECONDS));
        }

        assertNotNull(localParticipant.getLocalDataTracks().get(0));
        assertEquals(localDataTrack, localParticipant.getDataTracks().get(0).getDataTrack());
        assertTrue(localParticipant.unpublishTrack(localDataTrack));
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void connect_shouldAllowAudioVideoAndDataTracks() throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        List<LocalAudioTrack> localAudioTrackList =
                new ArrayList<LocalAudioTrack>(){{ add(localAudioTrack); }};
        List<LocalVideoTrack> localVideoTrackList =
                new ArrayList<LocalVideoTrack>(){{ add(localVideoTrack); }};
        List<LocalDataTrack> localDataTrackList =
                new ArrayList<LocalDataTrack>(){{ add(localDataTrack); }};
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTrackList)
                .videoTracks(localVideoTrackList)
                .dataTracks(localDataTrackList)
                .build();

        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        localParticipant.setListener(localParticipantListener);
        assertNotNull(localParticipant.getLocalAudioTracks().get(0));
        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0).getAudioTrack());
        assertNotNull(localParticipant.getLocalVideoTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0).getVideoTrack());
        if (topology == Topology.GROUP) {
            assertTrue(localParticipantListener.onPublishedDataTrackLatch.await(20,
                    TimeUnit.SECONDS));
        }
        assertNotNull(localParticipant.getLocalDataTracks().get(0));
        assertEquals(localDataTrack, localParticipant.getDataTracks().get(0).getDataTrack());
        assertTrue(localParticipant.unpublishTrack(localAudioTrack));
        assertTrue(localParticipant.unpublishTrack(localVideoTrack));
        assertTrue(localParticipant.unpublishTrack(localDataTrack));
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
        assertEquals(localVideoTrack,
                room.getLocalParticipant().getLocalVideoTracks().get(0).getLocalVideoTrack());
        assertTrue(room.getLocalParticipant()
                .getLocalVideoTracks().get(0).getLocalVideoTrack().isReleased());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void connect_shouldAllowLocalDataTrackToBeReleasedWhileConnecting()
            throws InterruptedException {
        CallbackHelper.FakeLocalParticipantListener localParticipantListener =
                new CallbackHelper.FakeLocalParticipantListener();
        localParticipantListener.onPublishedDataTrackLatch = new CountDownLatch(1);
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        List<LocalDataTrack> localDataTracks = Collections.singletonList(localDataTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .dataTracks(localDataTracks)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);

        // Add sleep to ensure that connect has started
        Thread.sleep(200);

        localDataTrack.release();
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        room.getLocalParticipant().setListener(localParticipantListener);
        if (topology == Topology.GROUP) {
            assertTrue(localParticipantListener.onPublishedDataTrackLatch.await(20,
                    TimeUnit.SECONDS));
        }
        assertEquals(localDataTrack,
                room.getLocalParticipant().getLocalDataTracks().get(0).getLocalDataTrack());
        assertTrue(room.getLocalParticipant()
                .getLocalDataTracks().get(0).getLocalDataTrack().isReleased());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void connect_shouldAllowLocalDataTrackToBeReleasedAfterConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        localDataTrack = LocalDataTrack.create(mediaTestActivity);
        List<LocalDataTrack> localDataTracks = Collections.singletonList(localDataTrack);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .dataTracks(localDataTracks)
                .build();
        room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        localDataTrack.release();
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
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
