package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.Topology;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class ParticipantTopologyParameterizedTest extends BaseClientTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.SFU}});
    }

    private Context context;
    private String tokenOne;
    private String tokenTwo;
    private String roomName;
    private final Topology topology;

    public ParticipantTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        tokenOne = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        tokenTwo = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
        roomName = RandUtils.generateRandomString(20);
    }

    @Test
    public void participantCanConnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(tokenOne)
                .roomName(roomName)
                .build();
        Room room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        connectOptions = new ConnectOptions.Builder(tokenTwo)
            .roomName(roomName)
            .build();
        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        Room room2 = Video.connect(context, connectOptions, roomListener2);
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getParticipants().size());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());
        room2.disconnect();
    }

    @Test
    public void participantCanDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(tokenOne)
                .roomName(roomName)
                .build();
        Room room = Video.connect(context, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        ConnectOptions connectOptions2 = new ConnectOptions.Builder(tokenTwo)
            .roomName(roomName)
            .build();
        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        roomListener2.onConnectedLatch = new CountDownLatch(1);
        roomListener2.onDisconnectedLatch = new CountDownLatch(1);
        Room client2room = Video.connect(context, connectOptions2, roomListener2);

        assertTrue(roomListener2.onConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client2Participants = new ArrayList<>(client2room.getParticipants()
                .values());
        Participant client1Participant = client2Participants.get(0);

        assertEquals(1, client2Participants.size());
        assertTrue(client1Participant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client1Participants = new ArrayList<>(room.getParticipants().values());
        Participant client2Participant = client1Participants.get(0);

        assertEquals(1, client1Participants.size());
        assertTrue(client2Participant.isConnected());

        client2room.disconnect();
        assertTrue(roomListener2.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client2Participant.isConnected());
        assertTrue(room.getParticipants().isEmpty());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client1Participant.isConnected());
    }
}
