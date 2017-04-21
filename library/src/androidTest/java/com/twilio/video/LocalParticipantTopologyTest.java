package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class LocalParticipantTopologyTest extends BaseClientTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Topology.P2P},
                {Topology.SFU}});
    }

    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String identity;
    private String token;
    private String roomName;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private final Topology topology;

    public LocalParticipantTopologyTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        identity = Constants.PARTICIPANT_ALICE;
        token = CredentialsUtils.getAccessToken(identity, topology);
        roomName = RandUtils.generateRandomString(20);
    }

    @After
    public void teardown() {
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
    }

    @Test
    public void shouldAllowAddingAndRemovingTracksWhileConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        // Now we add our tracks
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);
        localParticipant.addAudioTrack(localAudioTrack);
        localParticipant.addVideoTrack(localVideoTrack);

        // Now remove them
        assertTrue(localParticipant.removeAudioTrack(localAudioTrack));
        assertTrue(localParticipant.removeVideoTrack(localVideoTrack));
        room.disconnect();
    }

    @Test
    public void shouldHaveTracksWhenConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, fakeVideoCapturer);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .audioTracks(Collections.singletonList(localAudioTrack))
                .videoTracks(Collections.singletonList(localVideoTrack))
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        assertEquals(1, localParticipant.getAudioTracks().size());
        assertEquals(1, localParticipant.getVideoTracks().size());

        assertEquals(localAudioTrack, localParticipant.getAudioTracks().get(0));
        assertEquals(localVideoTrack, localParticipant.getVideoTracks().get(0));

        room.disconnect();
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidOnceConnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidUponDisconnect() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        room.disconnect();
        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
    }

    @Test
    public void shouldHaveIdentityAndNonNullSidOnceDisconnected() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .build();
        Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        LocalParticipant localParticipant = room.getLocalParticipant();
        assertNotNull(localParticipant);

        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(identity, localParticipant.getIdentity());
        assertNotNull(localParticipant.getSid());
    }

}
