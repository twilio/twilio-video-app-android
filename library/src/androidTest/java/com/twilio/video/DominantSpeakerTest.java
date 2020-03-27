/*
 * Copyright (C) 2019 Twilio, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.model.VideoRoom;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DominantSpeakerTest extends BaseParticipantTest {
    private VideoRoom videoRoom;

    private static final String AUDIO_WAV_FILE_NAME = "audio_dtx16.wav";
    private static final String AUDIO_WAV_FILE_INTERNAL_STORAGE_PATH_TEMPLATE = "%s/%s";
    private static String audioFilePath;

    private static File audioFile;

    @BeforeClass
    public static void copyAudioFile() {
        Context context = InstrumentationRegistry.getContext();
        AssetManager assetManager = context.getAssets();
        String internalStoragePath = context.getFilesDir().getAbsolutePath();
        try (InputStream is = assetManager.open(AUDIO_WAV_FILE_NAME)) {
            String audioFilePathString =
                    String.format(
                            AUDIO_WAV_FILE_INTERNAL_STORAGE_PATH_TEMPLATE,
                            internalStoragePath,
                            AUDIO_WAV_FILE_NAME);
            audioFile = new File(audioFilePathString);
            final int BUFFER_SIZE = 4 * 1024;
            try {
                try (OutputStream output = new FileOutputStream(audioFile)) {
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int read;

                    while ((read = is.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }

                    output.flush();
                }
            } finally {
                is.close();
            }
            audioFilePath = audioFile.getPath();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        testRoomName = RandomStringUtils.random(Constants.ROOM_NAME_LENGTH);
        mediaTestActivity = activityRule.getActivity();

        aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE);
        bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB);
        charlieToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_CHARLIE);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        if (videoRoom != null) {
            RoomUtils.completeRoom(videoRoom);
        }
        assertTrue(MediaFactory.isReleased());
    }

    @AfterClass
    public static void removeAudioFile() throws IOException {
        assertTrue(audioFile.delete());
    }

    @Test
    public void canObserveDominantSpeaker() throws InterruptedException {
        MediaOptions mediaOptions =
                new MediaOptions.Builder().enableH264(false).audioFilePath(audioFilePath).build();
        MediaFactory mediaFactory = MediaFactory.testCreate(mediaTestActivity, mediaOptions);

        bobLocalAudioTrack =
                mediaFactory.createAudioTrack(mediaTestActivity, true, null, "bob_audio_track");
        ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .build();

        ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(bobToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .audioTracks(Collections.singletonList(bobLocalAudioTrack))
                        .enableDominantSpeaker(true)
                        .build();

        aliceRoomListener = new CallbackHelper.FakeRoomListener();
        bobRoomListener = new CallbackHelper.FakeRoomListener();

        aliceRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);

        aliceRoom = connect(aliceConnectOptions, aliceRoomListener);
        bobRoom = connect(bobConnectOptions, bobRoomListener);

        assertTrue(
                aliceRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNotNull(aliceRoom.getDominantSpeaker());

        assertEquals(
                aliceRoomListener.getDominantSpeaker().getSid(),
                bobRoom.getLocalParticipant().getSid());

        assertEquals(
                aliceRoom.getDominantSpeaker().getSid(), bobRoom.getLocalParticipant().getSid());
        mediaFactory.testRelease();
    }

    @Test
    public void canObserveDominantSpeakerFromMultipleParticipants() throws InterruptedException {
        MediaOptions mediaOptions =
                new MediaOptions.Builder().enableH264(false).audioFilePath(audioFilePath).build();
        MediaFactory mediaFactory = MediaFactory.testCreate(mediaTestActivity, mediaOptions);

        charlieLocalAudioTrack =
                mediaFactory.createAudioTrack(mediaTestActivity, true, null, "charlie_audio_track");
        ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .build();

        ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(bobToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .build();

        ConnectOptions charlieConnectOptions =
                new ConnectOptions.Builder(charlieToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .audioTracks(Collections.singletonList(charlieLocalAudioTrack))
                        .build();

        aliceRoomListener = new CallbackHelper.FakeRoomListener();
        bobRoomListener = new CallbackHelper.FakeRoomListener();
        charlieRoomListener = new CallbackHelper.FakeRoomListener();

        aliceRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);
        bobRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);

        aliceRoom = connect(aliceConnectOptions, aliceRoomListener);
        bobRoom = connect(bobConnectOptions, bobRoomListener);
        charlieRoom = connect(charlieConnectOptions, charlieRoomListener);

        assertTrue(
                aliceRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                bobRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNotNull(aliceRoom.getDominantSpeaker());
        assertNotNull(bobRoom.getDominantSpeaker());

        assertEquals(
                aliceRoom.getDominantSpeaker().getSid(),
                charlieRoom.getLocalParticipant().getSid());
        assertEquals(
                bobRoom.getDominantSpeaker().getSid(), charlieRoom.getLocalParticipant().getSid());

        mediaFactory.testRelease();
    }

    @Test
    public void shouldNotReceiveCallbackInP2P() throws InterruptedException {
        Topology topology = Topology.P2P;
        videoRoom = RoomUtils.createRoom(testRoomName, topology);
        assertNotNull(videoRoom);

        MediaOptions mediaOptions =
                new MediaOptions.Builder().enableH264(false).audioFilePath(audioFilePath).build();
        MediaFactory mediaFactory = MediaFactory.testCreate(mediaTestActivity, mediaOptions);

        bobLocalAudioTrack =
                mediaFactory.createAudioTrack(mediaTestActivity, true, null, "bob_audio_track");
        ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .build();

        ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(bobToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .audioTracks(Collections.singletonList(bobLocalAudioTrack))
                        .enableDominantSpeaker(true)
                        .build();

        aliceRoomListener = new CallbackHelper.FakeRoomListener();
        bobRoomListener = new CallbackHelper.FakeRoomListener();

        aliceRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);

        aliceRoom = connect(aliceConnectOptions, aliceRoomListener);
        bobRoom = connect(bobConnectOptions, bobRoomListener);

        assertFalse(
                aliceRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNull(aliceRoom.getDominantSpeaker());

        mediaFactory.testRelease();
    }

    @Test
    public void canObserveDominantSpeakerAfterReconnected() throws InterruptedException {
        Topology topology = Topology.GROUP;

        MediaOptions mediaOptions =
                new MediaOptions.Builder().enableH264(false).audioFilePath(audioFilePath).build();
        MediaFactory mediaFactory = MediaFactory.testCreate(mediaTestActivity, mediaOptions);

        bobLocalAudioTrack =
                mediaFactory.createAudioTrack(mediaTestActivity, false, null, "bob_audio_track");
        charlieLocalAudioTrack =
                mediaFactory.createAudioTrack(mediaTestActivity, true, null, "charlie_audio_track");
        ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .build();

        ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(bobToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .enableDominantSpeaker(true)
                        .mediaFactory(mediaFactory)
                        .audioTracks(Collections.singletonList(bobLocalAudioTrack))
                        .build();

        ConnectOptions charlieConnectOptions =
                new ConnectOptions.Builder(charlieToken)
                        .iceOptions(
                                new IceOptions.Builder()
                                        .abortOnIceServersTimeout(true)
                                        .iceServersTimeout(TestUtils.ICE_TIMEOUT)
                                        .build())
                        .roomName(testRoomName)
                        .mediaFactory(mediaFactory)
                        .enableDominantSpeaker(true)
                        .audioTracks(Collections.singletonList(charlieLocalAudioTrack))
                        .build();

        aliceRoomListener = new CallbackHelper.FakeRoomListener();
        bobRoomListener = new CallbackHelper.FakeRoomListener();
        charlieRoomListener = new CallbackHelper.FakeRoomListener();

        aliceRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);
        bobRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);

        aliceRoom = connect(aliceConnectOptions, aliceRoomListener);
        bobRoom = connect(bobConnectOptions, bobRoomListener);
        charlieRoom = connect(charlieConnectOptions, charlieRoomListener);

        assertTrue(
                aliceRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                bobRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        assertNotNull(aliceRoom.getDominantSpeaker());
        assertNotNull(bobRoom.getDominantSpeaker());

        assertEquals(
                aliceRoom.getDominantSpeaker().getSid(),
                charlieRoom.getLocalParticipant().getSid());
        assertEquals(
                bobRoom.getDominantSpeaker().getSid(), charlieRoom.getLocalParticipant().getSid());

        aliceRoomListener.onReconnectedLatch = new CountDownLatch(1);
        aliceRoomListener.onDominantSpeakerChangedLatch = new CountDownLatch(1);
        aliceRoom.onNetworkChanged(Video.NetworkChangeEvent.CONNECTION_CHANGED);

        assertTrue(
                aliceRoomListener.onReconnectedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        charlieLocalAudioTrack.enable(false);
        bobLocalAudioTrack.enable(true);

        assertTrue(
                aliceRoomListener.onDominantSpeakerChangedLatch.await(
                        TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        mediaFactory.testRelease();
    }
}
