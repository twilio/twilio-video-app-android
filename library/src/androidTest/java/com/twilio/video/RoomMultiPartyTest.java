package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.AccessTokenUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RoomMultiPartyTest extends BaseClientTest {
    private static final int PARTICIPANT_NUM = 3;
    private static final String[] PARTICIPANTS = {
            Constants.PARTICIPANT_ALICE, Constants.PARTICIPANT_BOB, Constants.PARTICIPANT_CHARLIE
    };

    private Context context;
    private List<VideoClient> videoClients;
    private List<Pair<Room, CallbackHelper.FakeRoomListener>> rooms;
    private String roomName;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        rooms = new ArrayList<>();
        videoClients = new ArrayList<>();
        for (int i = 0; i < PARTICIPANT_NUM; i++) {
            String token = AccessTokenUtils.getAccessToken(PARTICIPANTS[i]);
            videoClients.add(new VideoClient(context, token));
        }
        roomName = RandUtils.generateRandomString(20);
    }

    @After
    public void teardown() throws InterruptedException {
        for (Pair<Room, CallbackHelper.FakeRoomListener> roomPair : rooms) {
            roomPair.second.onDisconnectedLatch = new CountDownLatch(1);
            roomPair.first.disconnect();
            roomPair.second.onDisconnectedLatch.await(10, TimeUnit.SECONDS);
        }
        rooms.clear();
    }

    @Test
    public void shouldHaveCorrectParticipantCount() throws InterruptedException {
        for (VideoClient client : videoClients) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onConnectedLatch = new CountDownLatch(1);
            int numberOfParticipants = rooms.size();

            // add listener to all other PARTICIPANTS
            for (Pair<Room, CallbackHelper.FakeRoomListener> roomPair : rooms) {
                roomPair.second.onParticipantConnectedLatch = new CountDownLatch(1);
            }

            Room room = createRoom(client, roomListener, roomName);
            assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
            assertEquals(numberOfParticipants, room.getParticipants().size());

            // check if all PARTICIPANTS got notification
            for (Pair<Room, CallbackHelper.FakeRoomListener> roomPair : rooms) {
                assertTrue(roomPair.second.onParticipantConnectedLatch.await(10, TimeUnit.SECONDS));
                assertEquals(numberOfParticipants, roomPair.first.getParticipants().size());
            }

            rooms.add(new Pair<Room, CallbackHelper.FakeRoomListener>(room, roomListener));
        }
    }

    @Test
    public void shouldNotHaveLocalParticipantInParticipantsList() throws InterruptedException {
        for (VideoClient client : videoClients) {
            CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
            roomListener.onConnectedLatch = new CountDownLatch(1);

            Room room = createRoom(client, roomListener, roomName);
            String localIdentity = room.getLocalParticipant().getIdentity();
            String localSid = room.getLocalParticipant().getSid();

            Map<String, Participant> participantMap = room.getParticipants();
            assertFalse(participantMap.containsKey(localIdentity));
            for (Participant participant : participantMap.values()) {
                assertNotEquals(localSid, participant.getSid());
            }
            rooms.add(new Pair<Room, CallbackHelper.FakeRoomListener>(room, roomListener));
        }
    }

    private Room createRoom(VideoClient videoClient, CallbackHelper.FakeRoomListener listener,
                            String roomName) throws InterruptedException {
        listener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .build();
        Room room = videoClient.connect(connectOptions, listener);
        assertTrue(listener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        return room;
    }
}
