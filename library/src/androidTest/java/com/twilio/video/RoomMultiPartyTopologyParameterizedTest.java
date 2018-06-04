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

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.util.Pair;

import com.twilio.video.base.BaseVideoTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class RoomMultiPartyTopologyParameterizedTest extends BaseVideoTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
    }

    private static final int PARTICIPANT_NUM = 3;
    private static final String[] PARTICIPANTS = {
            Constants.PARTICIPANT_ALICE, Constants.PARTICIPANT_BOB, Constants.PARTICIPANT_CHARLIE
    };

    private Context context;
    private List<String> tokens;
    private List<Pair<Room, CallbackHelper.FakeRoomListener>> rooms;
    private String roomName;
    private final Topology topology;

    public RoomMultiPartyTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        roomName = random(Constants.ROOM_NAME_LENGTH);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        rooms = new ArrayList<>();
        tokens = new ArrayList<>();
        for (int i = 0; i < PARTICIPANT_NUM; i++) {
            tokens.add(CredentialsUtils.getAccessToken(PARTICIPANTS[i], topology));
        }
    }

    @After
    public void teardown() throws InterruptedException {
        for (Pair<Room, CallbackHelper.FakeRoomListener> roomPair : rooms) {
            roomPair.second.onDisconnectedLatch = new CountDownLatch(1);
            roomPair.first.disconnect();
            roomPair.second.onDisconnectedLatch.await(10, TimeUnit.SECONDS);
        }
        rooms.clear();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void shouldHaveCorrectParticipantCount() throws InterruptedException {
        for (String token : tokens) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onConnectedLatch = new CountDownLatch(1);
            int numberOfParticipants = rooms.size();

            // add listener to all other participants
            for (Pair<Room, CallbackHelper.FakeRoomListener> roomPair : rooms) {
                roomPair.second.onParticipantConnectedLatch = new CountDownLatch(1);
            }

            Room room = createRoom(token, roomListener, roomName);
            assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
            assertEquals(numberOfParticipants, room.getRemoteParticipants().size());

            // check if all participants got notification
            for (Pair<Room, CallbackHelper.FakeRoomListener> roomPair : rooms) {
                assertTrue(roomPair.second.onParticipantConnectedLatch.await(10, TimeUnit.SECONDS));
                assertEquals(numberOfParticipants, roomPair.first.getRemoteParticipants().size());
            }

            rooms.add(new Pair<>(room, roomListener));
        }
    }

    @Test
    public void shouldNotHaveLocalParticipantInParticipantsList() throws InterruptedException {
        for (String token : tokens) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onConnectedLatch = new CountDownLatch(1);

            Room room = createRoom(token, roomListener, roomName);
            String localIdentity = room.getLocalParticipant().getIdentity();
            String localSid = room.getLocalParticipant().getSid();

            List<RemoteParticipant> remoteParticipants = room.getRemoteParticipants();
            for (RemoteParticipant remoteParticipant : remoteParticipants) {
                assertNotEquals(localSid, remoteParticipant.getSid());
            }
            rooms.add(new Pair<>(room, roomListener));
        }
    }

    private Room createRoom(String token, CallbackHelper.FakeRoomListener listener,
                            String roomName) throws InterruptedException {
        listener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(context, connectOptions, listener);
        assertTrue(listener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        return room;
    }
}
