package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.common.AccessManager;
import com.twilio.video.activity.RoomsTestActivity;
import com.twilio.video.helper.AccessTokenHelper;
import com.twilio.video.helper.CallbackHelper;
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

    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule = new ActivityTestRule<>(
            RoomsTestActivity.class);

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        localMedia = LocalMedia.create(context);
        fakeVideoCapturer = new FakeVideoCapturer();
        testRoom = RandUtils.generateRandomString(10);
        VideoClient.setLogLevel(LogLevel.DEBUG);
    }

    @After
    public void teardown() {
        localMedia.release();
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

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);

        videoClient.connect(roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldConnectToANamedRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .build();

        videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void connect_shouldAllowLocalMedia() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudio() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
        localMedia.removeAudioTrack(localAudioTrack);
    }

    @Test
    public void connect_shouldAllowLocalMediaWithVideo() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
        localMedia.removeLocalVideoTrack(localVideoTrack);
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudioAndVideo() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(false);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(false, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);

        VideoClient videoClient = new VideoClient(context, accessManager);
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .name(testRoom)
                .localMedia(localMedia)
                .build();

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalMedia());
        localMedia.removeAudioTrack(localAudioTrack);
        localMedia.removeLocalVideoTrack(localVideoTrack);
    }

    @Test
    public void connect_shouldDisconnectFromRoom() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
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
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        roomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        String randomRoomName = testRoom + System.currentTimeMillis();

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        AccessManager accessManager2 = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        VideoClient videoClient = new VideoClient(context, accessManager);
        VideoClient videoClient2 = new VideoClient(context, accessManager2);

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        videoClient2.connect(connectOptions, new CallbackHelper.EmptyRoomListener());

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

        AccessManager accessManager = AccessTokenHelper.obtainAccessManager(context, TEST_USER);
        AccessManager accessManager2 = AccessTokenHelper.obtainAccessManager(context, TEST_USER2);
        ConnectOptions connectOptions = new ConnectOptions.Builder().name(randomRoomName).build();

        VideoClient videoClient = new VideoClient(context, accessManager);
        VideoClient videoClient2 = new VideoClient(context, accessManager2);

        Room room = videoClient.connect(connectOptions, roomListener);

        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        Room client2room = videoClient2.connect(connectOptions, new CallbackHelper.EmptyRoomListener());

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

