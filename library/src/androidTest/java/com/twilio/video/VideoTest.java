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

import static junit.framework.TestCase.assertNotNull;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.twilioapi.model.VideoRoom;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class VideoTest extends BaseVideoTest {
    @Rule
    public GrantPermissionRule recordAudioPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO);

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    private MediaTestActivity mediaTestActivity;
    private String token;
    private String roomName;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private CallbackHelper.FakeRoomListener roomListener;
    private VideoRoom videoRoom;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomListener = new CallbackHelper.FakeRoomListener();
        roomName = random(Constants.ROOM_NAME_LENGTH);
        videoRoom = RoomUtils.createRoom(roomName, Topology.P2P);
        assertNotNull(videoRoom);
        token = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, Topology.P2P);
    }

    @After
    public void teardown() {
        RoomUtils.completeRoom(videoRoom);
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test(expected = NullPointerException.class)
    public void moduleLogLevel_ShouldNotAcceptNullModule() {
        Video.setModuleLogLevel(null, LogLevel.ALL);
    }

    @Test(expected = NullPointerException.class)
    public void moduleLogLevel_ShouldNotAcceptNullLogLevel() {
        Video.setModuleLogLevel(LogModule.CORE, null);
    }

    @Test(expected = NullPointerException.class)
    public void logLevel_ShouldNotAcceptNullLogLevel() {
        Video.setLogLevel(null);
    }

    @Test
    public void logLevel_shouldBeRetained() {
        Video.setLogLevel(LogLevel.DEBUG);
        assertEquals(LogLevel.DEBUG, Video.getLogLevel());
    }

    @Test
    public void getVersion_shouldReturnValidSemVerFormattedVersion() {
        String semVerRegex =
                "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-"
                        + "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = Video.getVersion();

        assertNotNull(version);
        assertTrue(version.matches(semVerRegex));
    }

    @Test(expected = IllegalStateException.class)
    public void connect_shouldShouldFailIfLocalVideoTrackReleasedBeforeConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .videoTracks(localVideoTracks)
                        .build();
        localVideoTrack.release();
        Video.connect(mediaTestActivity, connectOptions, roomListener);
    }

    @Test(expected = IllegalStateException.class)
    public void connect_shouldShouldFailIfLocalAudioTrackReleasedBeforeConnect()
            throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        ConnectOptions connectOptions =
                new ConnectOptions.Builder(token)
                        .roomName(roomName)
                        .audioTracks(localAudioTracks)
                        .build();
        localAudioTrack.release();
        Video.connect(mediaTestActivity, connectOptions, roomListener);
    }

    @Test
    public void canConnectAndDisconnectRepeatedly() throws InterruptedException {
        int numIterations = 25;
        Room room = null;
        for (int i = 0; i < numIterations; i++) {
            roomListener.onDisconnectedLatch = new CountDownLatch(1);
            ConnectOptions connectOptions = new ConnectOptions.Builder(token).build();
            room = Video.connect(mediaTestActivity, connectOptions, roomListener);
            room.disconnect();
            assertTrue(
                    roomListener.onDisconnectedLatch.await(
                            TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        }
        /*
         * After all participants have disconnected complete the room to clean up backend
         * resources.
         */
        RoomUtils.completeRoom(room);
    }
}
