package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
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
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class ParticipantTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.GROUP}});
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
        super.baseSetup(topology);
        roomName = RandUtils.generateRandomString(20);
        assertNotNull(RoomUtils.createRoom(roomName, topology));
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        tokenOne = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, topology);
        tokenTwo = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_BOB, topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
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
        roomListener2.onDisconnectedLatch = new CountDownLatch(1);
        Room room2 = Video.connect(context, connectOptions, roomListener2);
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(1, room.getParticipants().size());

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(RoomState.DISCONNECTED, room.getState());
        room2.disconnect();
        assertTrue(roomListener2.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
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

        List<Participant> client2Participants = new ArrayList<>(client2room.getParticipants());
        Participant client1Participant = client2Participants.get(0);

        assertEquals(1, client2Participants.size());
        assertTrue(client1Participant.isConnected());
        assertTrue(roomListener.onParticipantConnectedLatch.await(20, TimeUnit.SECONDS));

        List<Participant> client1Participants = new ArrayList<>(room.getParticipants());
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

    @Test
    public void shouldReceiveTrackEvents() throws InterruptedException {
        // Audio track added
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participant.setListener(participantListener);
        actor2LocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(actor2Room.getLocalParticipant().addAudioTrack(actor2LocalAudioTrack));
        assertTrue(participantListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Audio track disabled
        participantListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        actor2LocalAudioTrack.enable(false);
        assertTrue(participantListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track enabled
        participantListener.onAudioTrackEnabledLatch = new CountDownLatch(1);
        actor2LocalAudioTrack.enable(true);
        assertTrue(participantListener.onAudioTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track removed
        participantListener.onAudioTrackRemovedLatch = new CountDownLatch(1);
        actor2Room.getLocalParticipant().removeAudioTrack(actor2LocalAudioTrack);
        assertTrue(participantListener.onAudioTrackRemovedLatch.await(20, TimeUnit.SECONDS));

        // Video track added
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        assertTrue(actor2Room.getLocalParticipant().addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Video track disabled
        participantListener.onVideoTrackDisabledLatch = new CountDownLatch(1);
        actor2LocalVideoTrack.enable(false);
        assertTrue(participantListener.onVideoTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Video track enabled
        participantListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        actor2LocalVideoTrack.enable(true);
        assertTrue(participantListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Video track removed
        participantListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        actor2Room.getLocalParticipant().removeVideoTrack(actor2LocalVideoTrack);
        assertTrue(participantListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldHaveTracksAfterDisconnected() throws InterruptedException {
        // Add audio and video tracks
        CallbackHelper.FakeParticipantListener participantListener =
                new CallbackHelper.FakeParticipantListener();
        participantListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participant.setListener(participantListener);
        actor2LocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, false);
        assertTrue(actor2Room.getLocalParticipant().addAudioTrack(actor2LocalAudioTrack));
        assertTrue(participantListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        participantListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        actor2LocalVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        assertTrue(actor2Room.getLocalParticipant().addVideoTrack(actor2LocalVideoTrack));
        assertTrue(participantListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Cache participant two tracks
        List<RemoteAudioTrack> remoteAudioTracks = actor1RoomListener.getParticipant().getSubscribedAudioTracks();
        List<RemoteVideoTrack> remoteVideoTracks = actor1RoomListener.getParticipant().getSubscribedVideoTracks();

        // Participant two disconnects
        actor1RoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        actor2Room.disconnect();
        assertTrue(actor1RoomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));
        Participant participant = actor1RoomListener.getParticipant();

        // Validate that disconnected participant has all tracks
        assertEquals(remoteAudioTracks.get(0).isEnabled(),
                participant.getSubscribedAudioTracks().get(0).isEnabled());
        assertEquals(remoteAudioTracks.get(0), participant.getSubscribedAudioTracks().get(0));
        assertEquals(remoteVideoTracks.get(0).isEnabled(),
                participant.getSubscribedVideoTracks().get(0).isEnabled());
        assertEquals(remoteVideoTracks.get(0), participant.getSubscribedVideoTracks().get(0));
    }
}
