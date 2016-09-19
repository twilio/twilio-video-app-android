package com.twilio.video;

import android.app.Instrumentation;
import android.content.Context;
import android.provider.Telephony;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.AccessManager;
import com.twilio.video.helper.AccessTokenHelper;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.RoomsTestActivity;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConnectTest {
    private final static String TEST_USER  = "TEST_USER";
    private final static String TEST_USER2 = "TEST_USER2";

    private Context context;
    private String testRoom;
    private LocalMedia localMedia;
    private FakeVideoCapturer fakeVideoCapturer;
    private VideoClient actor1VideoClient;
    private VideoClient actor2VideoClient;
    private AccessManager actor1AccessManager;
    private AccessManager actor2AccessManager;

    private void disconnectRoom(Room room, CallbackHelper.FakeRoomListener roomListener)
            throws InterruptedException {
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule = new ActivityTestRule<>(
            RoomsTestActivity.class);

    @Before
    public void setup() throws InterruptedException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        localMedia = LocalMedia.create(context);
        fakeVideoCapturer = new FakeVideoCapturer();
        testRoom = RandUtils.generateRandomString(10);
        VideoClient.setLogLevel(LogLevel.DEBUG);

        // Actor 1
        actor1AccessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                actor1VideoClient = new VideoClient(context, actor1AccessManager);
            }
        });

        // Actor 2
        actor2AccessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                actor2VideoClient = new VideoClient(context, actor2AccessManager);
            }
        });

    }

    @After
    public void teardown() {
        actor1VideoClient = null;
        actor2VideoClient = null;
        localMedia.release();
        localMedia = null;
        actor1AccessManager.dispose();
        actor1AccessManager = null;
        actor2AccessManager.dispose();
        actor2AccessManager = null;
        fakeVideoCapturer = null;
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenRoomListenerIsNull() {
        VideoClient videoClient = new VideoClient(context, accessManager());
        videoClient.connect(null);
    }

    @Test
    public void connect_shouldConnectToANewRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        Room room = actor1VideoClient.connect(roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getSid(), room.getName());

        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldConnectToANamedRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldAllowLocalMedia() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldGetLocalParticipant() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertEquals(null, room.getLocalParticipant());
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        assertEquals(actor1AccessManager.getIdentity(), localParticipant.getIdentity());
        assertEquals(localMedia, localParticipant.getLocalMedia());
        assertNotNull(localParticipant.getSid());
        assertTrue(!localParticipant.getSid().isEmpty());

        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudio() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));

        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldAllowLocalMediaWithVideo() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));

        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudioAndVideo() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(false);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(false, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));

        disconnectRoom(room, roomListener);
    }

    @Test
    public void shouldAllowAddingAndRemovingTracksWhileConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());

        // Now we add our tracks
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(false);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(false, fakeVideoCapturer);

        // Let them sit a bit
        Thread.sleep(1);

        // Now remove them
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));

        disconnectRoom(room, roomListener);
    }

    @Test
    public void connect_shouldDisconnectFromRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        Room room = actor1VideoClient.connect(roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        disconnectRoom(room, roomListener);
        assertEquals(RoomState.DISCONNECTED, room.getState());
    }

    @Test
    public void connect_shouldConnectParticipant() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = testRoom + System.currentTimeMillis();
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(randomRoomName)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        Room room2 = actor2VideoClient.connect(connectOptions, roomListener2);
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getParticipants().size());

        disconnectRoom(room, roomListener);
        assertEquals(RoomState.DISCONNECTED, room.getState());
        disconnectRoom(room2, roomListener2);
    }

    @Test
    public void connect_shouldDisconnectParticipant() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = testRoom + System.currentTimeMillis();
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(randomRoomName)
                .build();
        Room room = actor1VideoClient.connect(connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        CallbackHelper.FakeRoomListener roomListener2 = new CallbackHelper.FakeRoomListener();
        roomListener2.onConnectedLatch = new CountDownLatch(1);
        roomListener2.onDisconnectedLatch = new CountDownLatch(1);
        Room client2room = actor2VideoClient.connect(connectOptions, roomListener2);

        assertTrue(roomListener2.onConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client2Participants = new ArrayList<>(client2room.getParticipants().values());
        Participant client1Participant = client2Participants.get(0);

        assertEquals(1, client2Participants.size());
        assertTrue(client1Participant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client1Participants = new ArrayList<>(room.getParticipants().values());
        Participant client2Participant = client1Participants.get(0);

        assertEquals(1, client1Participants.size());
        assertTrue(client2Participant.isConnected());

        disconnectRoom(client2room, roomListener2);
        assertTrue(roomListener2.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client2Participant.isConnected());
        assertTrue(room.getParticipants().isEmpty());

        disconnectRoom(room, roomListener);
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertFalse(client1Participant.isConnected());
    }


    private AccessManager accessManager() {
        return new AccessManager(context, null, null);
    }

}

