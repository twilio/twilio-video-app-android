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

import static junit.framework.Assert.fail;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RemoteParticipantTest extends BaseVideoTest {
    private static final String TAG = "RemoteParticipantTest";

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);

    private MediaTestActivity mediaTestActivity;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
    }

    /*
     * Validate that when `Participant.setListener(...)` is called by the developer when
     * `Room.Listener.onParticipantConnected` callback is raised, all `Participant.Listener`
     * callbacks associated with this participant are correctly raised afterwards. In some cases
     * callbacks associated with `Participant.Listener` may be raised while the listener has not
     * yet been set in `Room.Listener.onParticipantConnected`. These events should still be
     * propagated after the listener is set.
     */
    @Test
    public void shouldReceiveTrackEventsIfListenerSetAfterEventReceived()
            throws InterruptedException {
        final String roomName = random(Constants.ROOM_NAME_LENGTH);
        Topology topology = Topology.P2P;
        Assert.assertNotNull(RoomUtils.createRoom(roomName, topology));
        String aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        final CountDownLatch aliceConnected = new CountDownLatch(1);
        final CountDownLatch aliceSeesBobConnected = new CountDownLatch(1);
        final CountDownLatch bobPublishedAudioTrack = new CountDownLatch(1);
        final CountDownLatch aliceReceivedBobAudioTrackAdded = new CountDownLatch(1);
        final CountDownLatch aliceDisconnected = new CountDownLatch(1);
        final ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(aliceToken).roomName(roomName).build();
        String[] expectedTestEvents =
                new String[] {
                    "aliceConnected",
                    "aliceSeesBobConnected",
                    "bobPublishesAudioTrack",
                    "aliceSetsListener",
                    "aliceReceivesBobAudioTrackAdded"
                };
        final CallbackHelper.FakeRoomListener bobRoomListener =
                new CallbackHelper.FakeRoomListener();
        String bobToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
        bobRoomListener.onConnectedLatch = new CountDownLatch(1);
        bobRoomListener.onDisconnectedLatch = new CountDownLatch(1);
        final AtomicReference<Room> bobRoom = new AtomicReference<>(null);
        final ConnectOptions bobConnectOptions =
                new ConnectOptions.Builder(bobToken).roomName(roomName).build();
        final List<String> testEvents = Collections.synchronizedList(new ArrayList<String>());
        final RemoteParticipant.Listener aliceRemoteParticipantListener =
                new RemoteParticipant.Listener() {
                    @Override
                    public void onAudioTrackPublished(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication) {
                        Log.d(TAG, "Alice received bob's audio track");
                        testEvents.add("aliceReceivesBobAudioTrackAdded");
                        aliceReceivedBobAudioTrackAdded.countDown();
                    }

                    @Override
                    public void onAudioTrackUnpublished(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackSubscribed(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                            RemoteAudioTrack remoteAudioTrack) {}

                    @Override
                    public void onAudioTrackSubscriptionFailed(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                            TwilioException twilioException) {}

                    @Override
                    public void onAudioTrackUnsubscribed(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication,
                            RemoteAudioTrack remoteAudioTrack) {}

                    @Override
                    public void onVideoTrackPublished(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackUnpublished(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackSubscribed(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                            RemoteVideoTrack remoteVideoTrack) {}

                    @Override
                    public void onVideoTrackSubscriptionFailed(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublication,
                            TwilioException twilioException) {}

                    @Override
                    public void onVideoTrackUnsubscribed(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublicatieon,
                            RemoteVideoTrack remoteVideoTrack) {}

                    @Override
                    public void onDataTrackPublished(
                            RemoteParticipant remoteParticipant,
                            RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackUnpublished(
                            RemoteParticipant remoteParticipant,
                            RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackSubscribed(
                            RemoteParticipant remoteParticipant,
                            RemoteDataTrackPublication remoteDataTrackPublication,
                            RemoteDataTrack remoteDataTrack) {}

                    @Override
                    public void onDataTrackSubscriptionFailed(
                            RemoteParticipant remoteParticipant,
                            RemoteDataTrackPublication remoteDataTrackPublication,
                            TwilioException twilioException) {}

                    @Override
                    public void onDataTrackUnsubscribed(
                            RemoteParticipant remoteParticipant,
                            RemoteDataTrackPublication remoteDataTrackPublication,
                            RemoteDataTrack remoteDataTrack) {}

                    @Override
                    public void onAudioTrackEnabled(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackDisabled(
                            RemoteParticipant remoteParticipant,
                            RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onVideoTrackEnabled(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackDisabled(
                            RemoteParticipant remoteParticipant,
                            RemoteVideoTrackPublication remoteVideoTrackPublication) {}
                };
        final Room.Listener aliceRoomListener =
                new Room.Listener() {
                    @Override
                    public void onConnected(Room room) {
                        testEvents.add("aliceConnected");
                        aliceConnected.countDown();
                    }

                    @Override
                    public void onConnectFailure(Room room, TwilioException twilioException) {}

                    @Override
                    public void onDisconnected(Room room, TwilioException twilioException) {
                        aliceDisconnected.countDown();
                    }

                    @Override
                    public void onParticipantConnected(
                            Room room, RemoteParticipant remoteParticipant) {
                        Log.d(TAG, "Alice sees bob connected");
                        testEvents.add("aliceSeesBobConnected");
                        aliceSeesBobConnected.countDown();

                        /*
                         * Sleep to create race condition between receiving events and setting listener
                         */
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            fail(e.getMessage());
                        }

                        Log.d(TAG, "Alice sets bobRemoteParticipant listener");
                        testEvents.add("aliceSetsListener");
                        remoteParticipant.setListener(aliceRemoteParticipantListener);
                    }

                    @Override
                    public void onParticipantDisconnected(
                            Room room, RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(Room room) {}

                    @Override
                    public void onRecordingStopped(Room room) {}
                };

        /*
         * Perform alice and bob operations on separate threads so we can correctly replicate
         * the race condition while we wait and validate the correct behavior on the test thread.
         */

        // Connect alice
        final AtomicReference<Room> aliceRoom = new AtomicReference<>(null);
        HandlerThread aliceThread = new HandlerThread("AliceThread");
        aliceThread.start();
        Handler aliceHandler = new Handler(aliceThread.getLooper());

        aliceHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        aliceRoom.set(
                                Video.connect(
                                        mediaTestActivity, aliceConnectOptions, aliceRoomListener));
                    }
                });
        assertTrue(aliceConnected.await(20, TimeUnit.SECONDS));

        // Connect bob
        final LocalAudioTrack bobAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        HandlerThread bobThread = new HandlerThread("BobThread");
        bobThread.start();
        Handler bobHandler = new Handler(bobThread.getLooper());
        bobHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        bobRoom.set(
                                Video.connect(
                                        mediaTestActivity, bobConnectOptions, bobRoomListener));
                    }
                });
        assertTrue(bobRoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(aliceSeesBobConnected.await(20, TimeUnit.SECONDS));

        // Publish audio track
        bobHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        // Publish audio track for bob
                        LocalParticipant bobLocalParticipant = bobRoom.get().getLocalParticipant();
                        bobLocalParticipant.publishTrack(bobAudioTrack);
                        testEvents.add("bobPublishesAudioTrack");
                        bobPublishedAudioTrack.countDown();
                        Log.d(TAG, "bob published audio track");
                    }
                });

        // Validate that alice received bob track event and the events happened as expected
        assertTrue(aliceReceivedBobAudioTrackAdded.await(20, TimeUnit.SECONDS));
        assertArrayEquals(expectedTestEvents, testEvents.toArray());

        // Teardown test scenario
        aliceRoom.get().disconnect();
        bobRoom.get().disconnect();
        assertTrue(aliceDisconnected.await(20, TimeUnit.SECONDS));
        assertTrue(bobRoomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        RoomUtils.completeRoom(aliceRoom.get());
        bobAudioTrack.release();
        aliceThread.quit();
        bobThread.quit();
    }
}
