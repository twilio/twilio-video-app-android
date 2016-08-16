package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.AccessManager;
import com.twilio.video.activity.RoomsTestActivity;
import com.twilio.video.helper.AccessTokenHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConnectTests {
    private final static String TEST_USER  = "TEST_USER";
    private final static String TEST_USER2 = "TEST_USER2";
    private final static String TEST_ROOM  = "TEST_ROOM";

    private Context context;

    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule = new ActivityTestRule<>(
            RoomsTestActivity.class);

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Client.setLogLevel(LogLevel.DEBUG);
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenRoomListenerIsNull() {
        Client client = new Client(context, accessManager());
        client.connect(null);
    }

    @Test
    public void connect_shouldConnectToANewRoom() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        Client client = new Client(context, accessManager);

        client.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldConnectToANamedRoom() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        Client client = new Client(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .createRoom(true)
                .name(TEST_ROOM)
                .build();

        client.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldDisconnectFromRoom() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        Client client = new Client(context, accessManager);

        Room room = client.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        room.disconnect();

        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());

    }

    @Test
    public void connect_shouldConnectAnotherParticipant() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = TEST_ROOM + System.currentTimeMillis();

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        AccessManager accessManager2 = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        Client client = new Client(context, accessManager);
        Client client2 = new Client(context, accessManager2);

        Room room = client.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        client2.connect(connectOptions, new EmptyRoomListener());

        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getParticipants().size(), 1);

        room.disconnect();

        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());

    }

    private AccessManager accessManager() {
        return new AccessManager(context, null, null);
    }

    class FakeRoomListener implements Room.Listener {

        public CountDownLatch onConnectedLatch;
        public CountDownLatch onConnectFailureLatch;
        public CountDownLatch onDisconnectedLatch;
        public CountDownLatch onParticipantConnectedLatch;
        public CountDownLatch onParticipantDisconnectedLatch;

        private void triggerLatch(CountDownLatch latch) {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void onConnected(Room room) {
            triggerLatch(onConnectedLatch);
        }

        @Override
        public void onConnectFailure(RoomsException error) {
            triggerLatch(onConnectFailureLatch);
        }

        @Override
        public void onDisconnected(Room room, RoomsException error) {
            triggerLatch(onDisconnectedLatch);
        }

        @Override
        public void onParticipantConnected(Room room, Participant participant) {
            triggerLatch(onParticipantConnectedLatch);
        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {
            triggerLatch(onParticipantDisconnectedLatch);
        }
    }

    class EmptyRoomListener implements Room.Listener {

        @Override
        public void onConnected(Room room) {

        }

        @Override
        public void onConnectFailure(RoomsException error) {

        }

        @Override
        public void onDisconnected(Room room, RoomsException error) {

        }

        @Override
        public void onParticipantConnected(Room room, Participant participant) {

        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {

        }
    }
}

