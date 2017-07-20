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
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.Collections;
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

    /*
     * Alice and bob fixed tracks are provided upon connect.
     */
    protected LocalVideoTrack aliceFixedLocalVideoTrack;
    protected LocalAudioTrack aliceFixedLocalAudioTrack;
    protected LocalVideoTrack bobFixedLocalVideoTrack;
    protected LocalAudioTrack bobFixedLocalAudioTrack;

    /*
     * Alice and bob publishable tracks represent media that can be published and unpublished from a
     * room. This contrasts the fixed tracks which are added on connect.
     */
    protected LocalVideoTrack alicePublishableLocalVideoTrack;
    protected LocalAudioTrack alicePublishableLocalAudioTrack;
    protected LocalVideoTrack bobPublishableLocalVideoTrack;
    protected LocalAudioTrack bobPublishableLocalAudioTrack;

    protected String aliceToken;
    protected String bobToken;
    protected Room aliceRoom;
    protected LocalParticipant aliceLocalParticipant;
    protected Room bobRoom;
    protected LocalParticipant bobLocalParticipant;
    protected RemoteParticipant remoteParticipant;
    protected String testRoomName;
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
        aliceParticipantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        aliceParticipantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        aliceRoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        aliceFixedLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceFixedLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions aliceConnectOptions = new ConnectOptions.Builder(aliceToken)
                .roomName(testRoomName)
                .audioTracks(Collections.singletonList(aliceFixedLocalAudioTrack))
                .videoTracks(Collections.singletonList(aliceFixedLocalVideoTrack))
                .build();

        // Setup bob
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
        bobRoomListener = new CallbackHelper.FakeRoomListener();
        bobParticipantListener = new CallbackHelper.FakeParticipantListener();
        bobParticipantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        bobParticipantListener.onSubscribedToVideoTrackLatch = new CountDownLatch(1);
        bobFixedLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        bobFixedLocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true,
                new FakeVideoCapturer());
        ConnectOptions bobConnectOptions = new ConnectOptions.Builder(bobToken)
                .roomName(testRoomName)
                .audioTracks(Collections.singletonList(bobFixedLocalAudioTrack))
                .videoTracks(Collections.singletonList(bobFixedLocalVideoTrack))
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
        List<RemoteParticipant> remoteParticipantList = new ArrayList<>(aliceRoom.getRemoteParticipants());
        assertEquals(1, remoteParticipantList.size());
        remoteParticipant = remoteParticipantList.get(0);
        remoteParticipant.setListener(aliceParticipantListener);
        assertNotNull(remoteParticipant);

        // Alice wait until all of bob tracks are added
        assertTrue(aliceParticipantListener.onSubscribedToAudioTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(aliceParticipantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobParticipantListener.onSubscribedToAudioTrackLatch.await(20, TimeUnit.SECONDS));
        assertTrue(bobParticipantListener.onSubscribedToVideoTrackLatch.await(20, TimeUnit.SECONDS));
    }

    @After
    public void teardown() throws InterruptedException{
        disconnect(bobRoom, bobRoomListener);
        bobRoom = null;
        disconnect(aliceRoom, aliceRoomListener);
        aliceRoom = null;
        aliceRoomListener = null;
        remoteParticipant = null;
        if (aliceFixedLocalAudioTrack != null) {
            aliceFixedLocalAudioTrack.release();
        }
        if (aliceFixedLocalVideoTrack != null) {
            aliceFixedLocalVideoTrack.release();
        }
        if (bobFixedLocalAudioTrack != null) {
            bobFixedLocalAudioTrack.release();
        }
        if (bobFixedLocalVideoTrack != null) {
            bobFixedLocalVideoTrack.release();
        }
        if (alicePublishableLocalAudioTrack != null) {
            alicePublishableLocalAudioTrack.release();
        }
        if (alicePublishableLocalVideoTrack != null) {
            alicePublishableLocalVideoTrack.release();
        }
        if (bobPublishableLocalAudioTrack != null) {
            bobPublishableLocalAudioTrack.release();
        }
        if (bobPublishableLocalVideoTrack != null) {
            bobPublishableLocalVideoTrack.release();
        }
    }
}
