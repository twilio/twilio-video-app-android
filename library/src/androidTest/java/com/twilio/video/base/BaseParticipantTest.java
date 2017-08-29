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

import android.support.test.rule.ActivityTestRule;

import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
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
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseParticipantTest extends BaseClientTest {
    @Rule public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    protected MediaTestActivity mediaTestActivity;

    protected LocalVideoTrack aliceLocalVideoTrack;
    protected LocalAudioTrack aliceLocalAudioTrack;
    protected LocalVideoTrack bobLocalVideoTrack;
    protected LocalAudioTrack bobLocalAudioTrack;
    protected String aliceToken;
    protected String bobToken;
    protected Room aliceRoom;
    protected LocalParticipant aliceLocalParticipant;
    protected Room bobRoom;
    protected LocalParticipant bobLocalParticipant;
    protected RemoteParticipant bobRemoteParticipant;
    protected String testRoomName;
    protected String bobAudioTrackName;
    protected String bobVideoTrackName;
    protected CallbackHelper.FakeRoomListener aliceRoomListener;
    protected CallbackHelper.FakeParticipantListener aliceParticipantListener;
    protected CallbackHelper.FakeRoomListener bobRoomListener;
    protected CallbackHelper.FakeParticipantListener bobParticipantListener;

    protected Room connect(ConnectOptions connectOptions,
                           CallbackHelper.FakeRoomListener roomListener)
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue("Failed to connect to room",
                roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
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
        PermissionUtils.allowPermissions(mediaTestActivity);

        // Setup room
        testRoomName = RandUtils.generateRandomString(10);
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
        bobAudioTrackName = RandUtils.generateRandomString(10);
        bobVideoTrackName = RandUtils.generateRandomString(10);
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
        bobRoom.getRemoteParticipants().get(0).setListener(bobParticipantListener);

        // Alice wait for bob to connect
        assertTrue(aliceRoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        bobLocalParticipant = bobRoom.getLocalParticipant();
        List<RemoteParticipant> remoteParticipantList =
                new ArrayList<>(aliceRoom.getRemoteParticipants());
        assertEquals(1, remoteParticipantList.size());
        bobRemoteParticipant = remoteParticipantList.get(0);
        bobRemoteParticipant.setListener(aliceParticipantListener);
        assertNotNull(bobRemoteParticipant);
    }

    @After
    public void teardown() throws InterruptedException{
        disconnect(bobRoom, bobRoomListener);
        bobRoom = null;
        disconnect(aliceRoom, aliceRoomListener);
        aliceRoom = null;
        aliceRoomListener = null;
        bobRemoteParticipant = null;
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
    }
}
