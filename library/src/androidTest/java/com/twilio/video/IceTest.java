package com.twilio.video;

import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.AccessTokenUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.ServiceTokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class IceTest extends BaseClientTest {
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
        new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String aliceToken, bobToken;
    private String roomName;
    private LocalMedia aliceLocalMedia, bobLocalMedia;


    @Before
    public void setup() throws InterruptedException {
        super.setup();
        VideoClient.setLogLevel(LogLevel.ALL);
        VideoClient.setModuleLogLevel(LogModule.SIGNALING, LogLevel.ALL);
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        aliceToken = AccessTokenUtils.getAccessToken(Constants.PARTICIPANT_ALICE);
        roomName = RandUtils.generateRandomString(20);
        aliceLocalMedia = LocalMedia.create(mediaTestActivity);
        bobToken = AccessTokenUtils.getAccessToken(Constants.PARTICIPANT_BOB);
        bobLocalMedia = LocalMedia.create(mediaTestActivity);
    }

    @After
    public void teardown() {
        aliceLocalMedia.release();
        bobLocalMedia.release();
    }

    @Test
    public void shouldConnectWithWrongIceServers() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);
        Set<IceServer> iceServers = new HashSet<>();
        iceServers.add(new IceServer("stun:foo.bar.address?transport=udp"));
        iceServers.add(new IceServer("turn:foo.bar.address:3478?transport=udp", "fake", "pass"));

        IceOptions iceOptions = new IceOptions.Builder()
            .iceServers(iceServers)
            .iceTransportPolicy(IceTransportPolicy.RELAY)
            .build();
        ConnectOptions connectOptions = new ConnectOptions.Builder(aliceToken)
            .roomName(roomName)
            .iceOptions(iceOptions)
            .localMedia(aliceLocalMedia)
            .build();

        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        room.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldConnectWithValidStunServers() throws InterruptedException {
        Set<IceServer> iceServers = new HashSet<>();
        iceServers.add(new IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun1.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun2.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun3.l.google.com:19302"));
        iceServers.add(new IceServer("stun:stun4.l.google.com:19302"));
        IceOptions iceOptions = new IceOptions.Builder()
            .iceServers(iceServers)
            .iceTransportPolicy(IceTransportPolicy.ALL)
            .build();
        aliceLocalMedia.addAudioTrack(true);

        ConnectOptions connectOptions = new ConnectOptions.Builder(aliceToken)
            .roomName(roomName)
            .iceOptions(iceOptions)
            .localMedia(aliceLocalMedia)
            .build();
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);
        roomListener.onDisconnectedLatch = new CountDownLatch(1);

        Room aliceRoom = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        aliceRoom.disconnect();
        assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldConnectWithValidTurnServers() throws InterruptedException {
        CallbackHelper.FakeRoomListener aliceListener = new CallbackHelper.FakeRoomListener();
        aliceListener.onConnectedLatch = new CountDownLatch(1);
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);

        // Get ice servers from Twilio Service Token
        Set<IceServer> iceServers = ServiceTokenUtil.getIceServers();

        IceOptions iceOptions = new IceOptions.Builder()
            .iceServers(iceServers)
            .iceTransportPolicy(IceTransportPolicy.RELAY)
            .build();
        aliceLocalMedia.addAudioTrack(true);
        ConnectOptions connectOptions = new ConnectOptions.Builder(aliceToken)
            .roomName(roomName)
            .iceOptions(iceOptions)
            .localMedia(aliceLocalMedia)
            .build();

        Room aliceRoom = VideoClient.connect(mediaTestActivity, connectOptions, aliceListener);
        assertTrue(aliceListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        bobLocalMedia.addAudioTrack(true);
        bobLocalMedia.addVideoTrack(true, new FakeVideoCapturer());

        connectOptions = new ConnectOptions.Builder(bobToken)
            .roomName(roomName)
            .iceOptions(iceOptions)
            .localMedia(bobLocalMedia)
            .build();
        CallbackHelper.FakeRoomListener bobListener = new CallbackHelper.FakeRoomListener();
        bobListener.onConnectedLatch = new CountDownLatch(1);
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        Room bobRoom = VideoClient.connect(mediaTestActivity, connectOptions, bobListener);
        assertTrue(bobListener.onConnectedLatch.await(10, TimeUnit.SECONDS));
        assertTrue(aliceListener.onParticipantConnectedLatch.await(10, TimeUnit.SECONDS));
        aliceRoom.getParticipants().entrySet().iterator().next()
            .getValue().getMedia().setListener(mediaListener);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(10, TimeUnit.SECONDS));
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(10, TimeUnit.SECONDS));

        aliceListener.onDisconnectedLatch = new CountDownLatch(1);
        bobListener.onDisconnectedLatch = new CountDownLatch(1);
        aliceRoom.disconnect();
        bobRoom.disconnect();
        assertTrue(aliceListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS));
        assertTrue(bobListener.onDisconnectedLatch.await(10, TimeUnit.SECONDS));
    }

}
