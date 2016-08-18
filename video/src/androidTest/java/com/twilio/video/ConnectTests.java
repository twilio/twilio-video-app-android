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
        VideoClient.setLogLevel(LogLevel.DEBUG);
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenRoomListenerIsNull() {
        VideoClient videoClient = new VideoClient(context, accessManager());
        videoClient.connect(null);
    }

    @Test
    public void connect_shouldConnectToANewRoom() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);

        videoClient.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldConnectToANamedRoom() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(TEST_ROOM)
                .build();

        videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldDisconnectFromRoom() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);

        Room room = videoClient.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        room.disconnect();

        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());

    }

    @Test
    public void connect_shouldConnectParticipant() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = TEST_ROOM + System.currentTimeMillis();

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        AccessManager accessManager2 = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        VideoClient videoClient = new VideoClient(context, accessManager);
        VideoClient videoClient2 = new VideoClient(context, accessManager2);

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        videoClient2.connect(connectOptions, new EmptyRoomListener());

        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getParticipants().size());

        room.disconnect();

        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());

    }

    @Test
    public void connect_shouldDisconnectParticipant() throws InterruptedException {
        FakeRoomListener roomListener = new FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = TEST_ROOM + System.currentTimeMillis();

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        AccessManager accessManager2 = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        VideoClient videoClient = new VideoClient(context, accessManager);
        VideoClient videoClient2 = new VideoClient(context, accessManager2);

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        Room client2room = videoClient2.connect(connectOptions, new EmptyRoomListener());

        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getParticipants().size(), 1);

        client2room.disconnect();

        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(room.getParticipants().isEmpty());

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
        public void onConnectFailure(VideoException error) {
            triggerLatch(onConnectFailureLatch);
        }

        @Override
        public void onDisconnected(Room room, VideoException error) {
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
        public void onConnectFailure(VideoException error) {

        }

        @Override
        public void onDisconnected(Room room, VideoException error) {

        }

        @Override
        public void onParticipantConnected(Room room, Participant participant) {

        }

        @Override
        public void onParticipantDisconnected(Room room, Participant participant) {

        }
    }
}

