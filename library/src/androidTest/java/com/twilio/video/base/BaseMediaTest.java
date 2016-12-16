package com.twilio.video.base;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalMedia;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.VideoClient;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.SimplerSignalingUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseMediaTest extends BaseClientTest {
    protected final static String TEST_USER  = "TEST_USER";
    protected final static String TEST_USER2  = "TEST_USER2";
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    protected LocalMedia actor1LocalMedia;
    protected LocalMedia actor2LocalMedia;
    protected FakeVideoCapturer fakeVideoCapturer;
    protected VideoClient actor1VideoClient;
    protected VideoClient actor2VideoClient;
    protected String tokenOne;
    protected String  tokenTwo;
    protected Room actor1Room;
    protected Room actor2Room;
    protected Participant participant;
    protected String testRoom;
    protected CallbackHelper.FakeRoomListener actor1RoomListener;
    protected CallbackHelper.FakeRoomListener actor2RoomListener;

    protected Room connectClient(VideoClient videoClient, LocalMedia localMedia,
                               Room.Listener roomListener) {
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
        return room;
    }

    protected void disconnectRoom(Room room, CallbackHelper.FakeRoomListener roomListener)
            throws InterruptedException {
        if (room == null || room.getState() == RoomState.DISCONNECTED) {
            return;
        }
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        testRoom = RandUtils.generateRandomString(10);
        fakeVideoCapturer = new FakeVideoCapturer();
        actor1LocalMedia = LocalMedia.create(mediaTestActivity);
        tokenOne = SimplerSignalingUtils.getAccessToken(TEST_USER);
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                actor1VideoClient = new VideoClient(mediaTestActivity, tokenOne);
            }
        });
        // Connect actor 1
        actor1RoomListener = new CallbackHelper.FakeRoomListener();
        actor1RoomListener.onConnectedLatch = new CountDownLatch(1);
        actor1RoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        actor1Room = connectClient(actor1VideoClient, actor1LocalMedia, actor1RoomListener);
        assertTrue(actor1RoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        // Connect actor 2
        actor2LocalMedia = LocalMedia.create(mediaTestActivity);
        tokenTwo = SimplerSignalingUtils.getAccessToken(TEST_USER2);
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                actor2VideoClient = new VideoClient(mediaTestActivity, tokenTwo);
            }
        });
        actor2RoomListener = new CallbackHelper.FakeRoomListener();
        actor2Room = connectClient(actor2VideoClient, actor2LocalMedia, actor2RoomListener);

        // Wait for actor2 to connect
        assertTrue(actor1RoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        List<Participant> participantList = new ArrayList<>(actor1Room.getParticipants().values());
        assertEquals(1, participantList.size());
        participant = participantList.get(0);
        assertNotNull(participant);
    }

    @After
    public void teardown() throws InterruptedException{
        disconnectRoom(actor2Room, actor2RoomListener);
        actor2Room = null;
        disconnectRoom(actor1Room, actor1RoomListener);
        actor1Room = null;
        actor1RoomListener = null;
        participant = null;
        actor1VideoClient = null;
        actor2VideoClient = null;
        actor1LocalMedia.release();
        actor1LocalMedia = null;
        actor2LocalMedia.release();
        actor2LocalMedia = null;
        fakeVideoCapturer = null;
    }
}
