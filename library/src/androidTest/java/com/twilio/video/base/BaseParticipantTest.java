package com.twilio.video.base;

import android.support.test.rule.ActivityTestRule;

import com.twilio.video.ConnectOptions;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.Participant;
import com.twilio.video.Room;
import com.twilio.video.RoomState;
import com.twilio.video.Video;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Rule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public abstract class BaseParticipantTest extends BaseClientTest {
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    protected MediaTestActivity mediaTestActivity;
    protected LocalVideoTrack actor1LocalVideoTrack;
    protected LocalAudioTrack actor1LocalAudioTrack;
    protected LocalVideoTrack actor2LocalVideoTrack;
    protected LocalAudioTrack actor2LocalAudioTrack;
    protected FakeVideoCapturer fakeVideoCapturer;
    protected String tokenOne;
    protected String tokenTwo;
    protected Room actor1Room;
    protected LocalParticipant actor1LocalParticipant;
    protected Room actor2Room;
    protected LocalParticipant actor2LocalParticipant;
    protected Participant participant;
    protected String testRoom;
    protected CallbackHelper.FakeRoomListener actor1RoomListener;
    protected CallbackHelper.FakeRoomListener actor2RoomListener;

    protected Room connectClient(String token, Room.Listener roomListener) {
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(testRoom)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
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

    public void baseSetup(Topology topology) throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        testRoom = RandUtils.generateRandomString(10);
        assertNotNull(RoomUtils.createRoom(testRoom, topology));
        fakeVideoCapturer = new FakeVideoCapturer();
        tokenOne = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);

        // Connect actor 1
        actor1RoomListener = new CallbackHelper.FakeRoomListener();
        actor1RoomListener.onConnectedLatch = new CountDownLatch(1);
        actor1RoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        actor1Room = connectClient(tokenOne, actor1RoomListener);
        assertTrue(actor1RoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        actor1LocalParticipant = actor1Room.getLocalParticipant();

        // Connect actor 2
        tokenTwo = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);

        actor2RoomListener = new CallbackHelper.FakeRoomListener();
        actor2RoomListener.onConnectedLatch = new CountDownLatch(1);
        actor2Room = connectClient(tokenTwo, actor2RoomListener);
        assertTrue(actor2RoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        // Wait for actor2 to connect
        assertTrue(actor1RoomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        actor2LocalParticipant = actor2Room.getLocalParticipant();
        List<Participant> participantList = new ArrayList<>(actor1Room.getParticipants());
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
        if (actor1LocalAudioTrack != null) {
            actor1LocalAudioTrack.release();
        }
        if (actor1LocalVideoTrack != null) {
            actor1LocalVideoTrack.release();
        }
        if (actor2LocalAudioTrack != null) {
            actor2LocalAudioTrack.release();
        }
        if (actor2LocalVideoTrack != null) {
            actor2LocalVideoTrack.release();
        }
        fakeVideoCapturer = null;
    }
}
