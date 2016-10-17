package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.AccessTokenUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RoomTest {
    private Context context;
    private String identity;
    private String token;
    private VideoClient videoClient;
    private String roomName;
    private LocalMedia localMedia;

    @Before
    public void setup() throws InterruptedException {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        identity = RandUtils.generateRandomString(10);
        token = AccessTokenUtils.getAccessToken(identity);
        videoClient = new VideoClient(context, token);
        roomName = RandUtils.generateRandomString(20);
        localMedia = LocalMedia.create(context);
    }

    @After
    public void teardown() {
        localMedia.release();
    }

    @Test
    public void shouldReturnLocalParticipantOnConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
        assertNull(room.getLocalParticipant());
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);
        assertEquals(identity, localParticipant.getIdentity());
        assertEquals(localMedia, localParticipant.getLocalMedia());
        assertNotNull(localParticipant.getSid());
        assertTrue(!localParticipant.getSid().isEmpty());
        room.disconnect();
    }

    @Test
    public void shouldAllowAddingAndRemovingTracksWhileConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
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
        room.disconnect();
    }

    @Test
    public void canDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        Room room = videoClient.connect(roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.CONNECTED, room.getState());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());
    }
}
