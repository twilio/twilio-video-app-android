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

package com.twilio.video.base;

import android.Manifest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalDataTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.Video;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public abstract class BaseParticipantTest extends BaseVideoTest {
    @Rule
    public GrantPermissionRule recordAudioPermissionRule = GrantPermissionRule
            .grant(Manifest.permission.RECORD_AUDIO);
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    protected MediaTestActivity mediaTestActivity;

    protected LocalVideoTrack aliceLocalVideoTrack;
    protected LocalAudioTrack aliceLocalAudioTrack;
    protected LocalDataTrack aliceLocalDataTrack;
    protected LocalVideoTrack bobLocalVideoTrack;
    protected LocalAudioTrack bobLocalAudioTrack;
    protected LocalDataTrack bobLocalDataTrack;
    protected String aliceToken;
    protected String bobToken;
    protected Room aliceRoom;
    protected LocalParticipant aliceLocalParticipant;
    protected RemoteParticipant aliceRemoteParticipant;
    protected Room bobRoom;
    protected LocalParticipant bobLocalParticipant;
    protected RemoteParticipant bobRemoteParticipant;
    protected String testRoomName;
    protected String bobAudioTrackName;
    protected String bobVideoTrackName;
    protected LocalDataTrack charlieLocalDataTrack;
    protected Room charlieRoom;
    protected CallbackHelper.FakeRoomListener aliceRoomListener;
    protected CallbackHelper.FakeParticipantListener aliceParticipantListener;
    protected CallbackHelper.FakeRoomListener bobRoomListener;
    protected CallbackHelper.FakeLocalParticipantListener bobLocalParticipantListener;
    protected CallbackHelper.FakeParticipantListener bobParticipantListener;
    protected CallbackHelper.FakeRoomListener charlieRoomListener =
            new CallbackHelper.FakeRoomListener();

    protected Room connect(ConnectOptions connectOptions,
                           CallbackHelper.FakeRoomListener roomListener)
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        boolean connected = roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS);

        // Call disconnect before failing to ensure native memory released
        if (!connected) {
            room.disconnect();

            fail("Failed to connect to room");
        }

        return room;
    }

    protected void disconnect(Room room, CallbackHelper.FakeRoomListener roomListener)
            throws InterruptedException {
        if (room == null || room.getState() == RoomState.DISCONNECTED) {
            return;
        }
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        room.disconnect();
        assertTrue("Failed to disconnect from room",
                roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    public void baseSetup(Topology topology) throws InterruptedException {
        super.setup();
        // Setup activity
        mediaTestActivity = activityRule.getActivity();
        // Setup room
        testRoomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(testRoomName, topology));

        // Setup alice
        aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        aliceRoomListener = new CallbackHelper.FakeRoomListener();
        aliceParticipantListener = new CallbackHelper.FakeParticipantListener();
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(testRoomName)
                .build();

        // Setup bob
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
        bobAudioTrackName = random(10);
        bobVideoTrackName = random(10);
        bobRoomListener = new CallbackHelper.FakeRoomListener();
        bobParticipantListener = new CallbackHelper.FakeParticipantListener();
        ConnectOptions bobConnectOptions = new ConnectOptions.Builder(bobToken)
                .roomName(testRoomName)
                .build();

        // Connect alice
        aliceRoom = connect(aliceConnectOptions, aliceRoomListener);
        aliceLocalParticipant = aliceRoom.getLocalParticipant();

        // Connect bob
        bobRoom = connect(bobConnectOptions, bobRoomListener);
        aliceRemoteParticipant = bobRoom.getRemoteParticipants().get(0);
        aliceRemoteParticipant.setListener(bobParticipantListener);

        // Alice wait for bob to connect
        assertTrue(aliceRoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        bobLocalParticipant = bobRoom.getLocalParticipant();
        List<RemoteParticipant> remoteParticipantList =
                new ArrayList<>(aliceRoom.getRemoteParticipants());
        assertEquals(1, remoteParticipantList.size());
        bobRemoteParticipant = remoteParticipantList.get(0);
        assertNotNull(bobRemoteParticipant);
        bobRemoteParticipant.setListener(aliceParticipantListener);
    }

    @After
    public void teardown() throws InterruptedException{
        disconnect(bobRoom, bobRoomListener);
        bobRoom = null;
        disconnect(aliceRoom, aliceRoomListener);
        aliceRoom = null;
        aliceRoomListener = null;
        disconnect(charlieRoom, charlieRoomListener);
        bobRemoteParticipant = null;
        if (aliceLocalAudioTrack != null) {
            aliceLocalAudioTrack.release();
        }
        if (aliceLocalVideoTrack != null) {
            aliceLocalVideoTrack.release();
        }
        if (aliceLocalDataTrack != null) {
            aliceLocalDataTrack.release();
        }
        if (bobLocalAudioTrack != null) {
            bobLocalAudioTrack.release();
        }
        if (bobLocalVideoTrack != null) {
            bobLocalVideoTrack.release();
        }
        if (bobLocalDataTrack != null) {
            bobLocalDataTrack.release();
        }
        if (charlieLocalDataTrack != null) {
            charlieLocalDataTrack.release();
        }
    }
}
