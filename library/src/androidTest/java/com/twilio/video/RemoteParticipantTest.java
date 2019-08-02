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

import static com.twilio.video.TestUtils.ICE_TIMEOUT;
import static com.twilio.video.TestUtils.STATE_TRANSITION_TIMEOUT;
import static junit.framework.Assert.fail;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.twilioapi.model.VideoRoom;
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
    private VideoRoom videoRoom;

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
        videoRoom = RoomUtils.createRoom(roomName, topology);
        Assert.assertNotNull(videoRoom);
        String aliceToken = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        final CountDownLatch aliceConnected = new CountDownLatch(1);
        final CountDownLatch aliceSeesBobConnected = new CountDownLatch(1);
        final CountDownLatch bobPublishedAudioTrack = new CountDownLatch(1);
        final CountDownLatch aliceReceivedBobAudioTrackAdded = new CountDownLatch(1);
        final CountDownLatch aliceDisconnected = new CountDownLatch(1);
        IceOptions iceOptions =
                new IceOptions.Builder()
                        .iceServersTimeout(ICE_TIMEOUT)
                        .abortOnIceServersTimeout(true)
                        .build();
        final ConnectOptions aliceConnectOptions =
                new ConnectOptions.Builder(aliceToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
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
                new ConnectOptions.Builder(bobToken)
                        .roomName(roomName)
                        .iceOptions(iceOptions)
                        .build();
        final List<String> testEvents = Collections.synchronizedList(new ArrayList<String>());
        final RemoteParticipant.Listener aliceRemoteParticipantListener =
                new RemoteParticipant.Listener() {
                    @Override
                    public void onAudioTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {
                        Log.d(TAG, "Alice received bob's audio track");
                        testEvents.add("aliceReceivesBobAudioTrackAdded");
                        aliceReceivedBobAudioTrackAdded.countDown();
                    }

                    @Override
                    public void onAudioTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull RemoteAudioTrack remoteAudioTrack) {}

                    @Override
                    public void onAudioTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onAudioTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication,
                            @NonNull RemoteAudioTrack remoteAudioTrack) {}

                    @Override
                    public void onVideoTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull RemoteVideoTrack remoteVideoTrack) {}

                    @Override
                    public void onVideoTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onVideoTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublicatieon,
                            @NonNull RemoteVideoTrack remoteVideoTrack) {}

                    @Override
                    public void onDataTrackPublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackUnpublished(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication) {}

                    @Override
                    public void onDataTrackSubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull RemoteDataTrack remoteDataTrack) {}

                    @Override
                    public void onDataTrackSubscriptionFailed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull TwilioException twilioException) {}

                    @Override
                    public void onDataTrackUnsubscribed(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteDataTrackPublication remoteDataTrackPublication,
                            @NonNull RemoteDataTrack remoteDataTrack) {}

                    @Override
                    public void onAudioTrackEnabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onAudioTrackDisabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteAudioTrackPublication remoteAudioTrackPublication) {}

                    @Override
                    public void onVideoTrackEnabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}

                    @Override
                    public void onVideoTrackDisabled(
                            @NonNull RemoteParticipant remoteParticipant,
                            @NonNull RemoteVideoTrackPublication remoteVideoTrackPublication) {}
                };
        final Room.Listener aliceRoomListener =
                new Room.Listener() {
                    @Override
                    public void onConnected(@NonNull Room room) {
                        testEvents.add("aliceConnected");
                        aliceConnected.countDown();
                    }

                    @Override
                    public void onConnectFailure(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnecting(
                            @NonNull Room room, @NonNull TwilioException twilioException) {}

                    @Override
                    public void onReconnected(@NonNull Room room) {}

                    @Override
                    public void onDisconnected(
                            @NonNull Room room, @Nullable TwilioException twilioException) {
                        aliceDisconnected.countDown();
                    }

                    @Override
                    public void onParticipantConnected(
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {
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
                            @NonNull Room room, @NonNull RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onDominantSpeakerChanged(
                            @NonNull Room room, @Nullable RemoteParticipant remoteParticipant) {}

                    @Override
                    public void onRecordingStarted(@NonNull Room room) {}

                    @Override
                    public void onRecordingStopped(@NonNull Room room) {}
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
                () ->
                        aliceRoom.set(
                                Video.connect(
                                        mediaTestActivity,
                                        aliceConnectOptions,
                                        aliceRoomListener)));
        assertTrue(aliceConnected.await(TestUtils.STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Connect bob
        final LocalAudioTrack bobAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        HandlerThread bobThread = new HandlerThread("BobThread");
        bobThread.start();
        Handler bobHandler = new Handler(bobThread.getLooper());
        bobHandler.post(
                () ->
                        bobRoom.set(
                                Video.connect(
                                        mediaTestActivity, bobConnectOptions, bobRoomListener)));
        assertTrue(
                bobRoomListener.onConnectedLatch.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(aliceSeesBobConnected.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));

        // Publish audio track
        bobHandler.post(
                () -> {
                    // Publish audio track for bob
                    LocalParticipant bobLocalParticipant = bobRoom.get().getLocalParticipant();
                    bobLocalParticipant.publishTrack(bobAudioTrack);
                    testEvents.add("bobPublishesAudioTrack");
                    bobPublishedAudioTrack.countDown();
                    Log.d(TAG, "bob published audio track");
                });

        // Validate that alice received bob track event and the events happened as expected
        assertTrue(
                aliceReceivedBobAudioTrackAdded.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertArrayEquals(expectedTestEvents, testEvents.toArray());

        // Teardown test scenario
        aliceRoom.get().disconnect();
        bobRoom.get().disconnect();
        assertTrue(aliceDisconnected.await(STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        assertTrue(
                bobRoomListener.onDisconnectedLatch.await(
                        STATE_TRANSITION_TIMEOUT, TimeUnit.SECONDS));
        /*
         * After all participants have disconnected complete the room to clean up backend
         * resources.
         */
        RoomUtils.completeRoom(aliceRoom.get());
        RoomUtils.completeRoom(videoRoom);
        bobAudioTrack.release();
        aliceThread.quit();
        bobThread.quit();
    }
}
