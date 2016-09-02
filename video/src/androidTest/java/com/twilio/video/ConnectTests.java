package com.twilio.video;

import android.app.Instrumentation;
import android.content.Context;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConnectTests {
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
        actor1VideoClient.release();
        actor1VideoClient = null;
        actor2VideoClient.release();
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

        actor1VideoClient.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldConnectToANamedRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);


        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .build();

        actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowLocalMedia() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudio() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
    }

    @Test
    public void connect_shouldAllowLocalMediaWithVideo() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudioAndVideo() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(false);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(false, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));
    }

    @Test
    public void shouldAllowAddingAndRemovingTracksWhileConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();

        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());

        // Now we add our tracks
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(false);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(false, fakeVideoCapturer);

        // Let them sit a bit
        Thread.sleep(1);

        // Now remove them
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));
    }

    @Test
    public void connect_shouldDisconnectFromRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);


        Room room = actor1VideoClient.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        room.disconnect();

        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());

    }

    @Test
    public void connect_shouldConnectParticipant() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = testRoom + System.currentTimeMillis();

        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        actor2VideoClient.connect(connectOptions, new CallbackHelper.EmptyRoomListener());

        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getParticipants().size());

        room.disconnect();

        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());

    }

    @Test
    public void connect_shouldDisconnectParticipant() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = testRoom + System.currentTimeMillis();

        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        Room room = actor1VideoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        Room client2room = actor2VideoClient.connect(connectOptions, new CallbackHelper.EmptyRoomListener());

        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getParticipants().size(), 1);

        client2room.disconnect();

        assertTrue(roomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(room.getParticipants().isEmpty());

    }


    private AccessManager accessManager() {
        return new AccessManager(context, null, null);
    }

}

