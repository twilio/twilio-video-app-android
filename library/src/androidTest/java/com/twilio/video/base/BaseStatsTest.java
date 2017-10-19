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
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;

import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.Video;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseStatsTest extends BaseClientTest {
    @Rule
    public GrantPermissionRule recordAudioPermissionRule = GrantPermissionRule
            .grant(Manifest.permission.RECORD_AUDIO);
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    protected MediaTestActivity mediaTestActivity;
    protected String aliceToken;
    protected String bobToken;
    protected String roomName;
    protected Room aliceRoom;
    protected Room bobRoom;
    protected LocalVideoTrack aliceLocalVideoTrack;
    protected LocalAudioTrack aliceLocalAudioTrack;
    protected LocalVideoTrack bobLocalVideoTrack;
    protected LocalAudioTrack bobLocalAudioTrack;
    protected CallbackHelper.FakeRoomListener aliceListener;
    protected CallbackHelper.FakeRoomListener bobListener;
    protected CallbackHelper.FakeParticipantListener aliceMediaListener;
    protected CallbackHelper.FakeParticipantListener bobMediaListener;
    protected Topology topology;

    protected void baseSetup(Topology topology) {
        mediaTestActivity = activityRule.getActivity();
        roomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
        aliceListener = new CallbackHelper.FakeRoomListener();
        aliceMediaListener = new CallbackHelper.FakeParticipantListener();
        bobMediaListener = new CallbackHelper.FakeParticipantListener();
        bobListener = new CallbackHelper.FakeRoomListener();
    }

    @CallSuper
    protected void teardown() throws InterruptedException {
        roomTearDown(aliceRoom);
        roomTearDown(bobRoom);
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

    protected Room createRoom(String token,
                              CallbackHelper.FakeRoomListener listener,
                              String roomName) throws InterruptedException {
        return createRoom(token, listener, roomName, null, null);
    }

    protected Room createRoom(String token,
                              CallbackHelper.FakeRoomListener listener,
                              String roomName,
                              List<LocalAudioTrack> audioTracks) throws InterruptedException {
        return createRoom(token, listener, roomName, audioTracks, null);
    }

    protected Room createRoom(String token,
                              CallbackHelper.FakeRoomListener listener,
                              String roomName,
                              @Nullable List<LocalAudioTrack> audioTracks,
                              @Nullable List<LocalVideoTrack> videoTracks)
            throws InterruptedException {
        if (audioTracks == null) {
            audioTracks = new ArrayList<>();
        }
        if (videoTracks == null) {
            videoTracks = new ArrayList<>();
        }
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(audioTracks)
                .videoTracks(videoTracks)
                .build();

        return createRoom(listener, connectOptions);
    }

    protected Room createRoom(CallbackHelper.FakeRoomListener listener,
                              ConnectOptions connectOptions) throws InterruptedException {
        listener.onConnectedLatch = new CountDownLatch(1);
        Room room = Video.connect(mediaTestActivity, connectOptions, listener);
        assertTrue(listener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        return room;
    }

    protected void roomTearDown(Room room) throws InterruptedException {
        if (room != null && room.getState() != RoomState.DISCONNECTED) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onDisconnectedLatch = new CountDownLatch(1);
            room.disconnect();
            roomListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS);
        }
    }
}
