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
import com.twilio.video.util.FakeVideoRenderer;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RemoteMediaTest {
    

    private final static String TEST_USER  = "TEST_USER";
    private final static String TEST_USER2  = "TEST_USER2";
    @Rule
    public ActivityTestRule<RoomsTestActivity> activityRule =
            new ActivityTestRule<>(RoomsTestActivity.class);

    private Context context;
    private LocalMedia actor1LocalMedia;
    private LocalMedia actor2LocalMedia;
    private FakeVideoCapturer fakeVideoCapturer;
    private VideoClient actor1VideoClient;
    private VideoClient actor2VideoClient;
    private AccessManager actor1AccessManager;
    private AccessManager actor2AccessManager;
    private Room actor1Room;
    private Room actor2Room;
    private Participant participant;
    private String testRoom;
    private CallbackHelper.FakeRoomListener actor1RoomListener;
    private CallbackHelper.FakeRoomListener actor2RoomListener;


    private Room connectClient(VideoClient videoClient, LocalMedia localMedia,
                               Room.Listener roomListener) {
        ConnectOptions connectOptions = new ConnectOptions.Builder()
                .roomName(testRoom)
                .localMedia(localMedia)
                .build();
        Room room = videoClient.connect(connectOptions, roomListener);
        return room;
    }

    private void disconnectRoom(Room room, CallbackHelper.FakeRoomListener roomListener)
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
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testRoom = RandUtils.generateRandomString(10);
        fakeVideoCapturer = new FakeVideoCapturer();
        actor1LocalMedia = LocalMedia.create(context);
        String token = AccessTokenHelper.obtainCapabilityToken(TEST_USER);
        actor1AccessManager = new AccessManager(context, token, null);
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                actor1VideoClient = new VideoClient(context, actor1AccessManager);
            }
        });
        // Connect actor 1
        actor1RoomListener = new CallbackHelper.FakeRoomListener();
        actor1RoomListener.onConnectedLatch = new CountDownLatch(1);
        actor1RoomListener.onParticipantConnectedLatch = new CountDownLatch(1);
        actor1Room = connectClient(actor1VideoClient, actor1LocalMedia, actor1RoomListener);
        assertTrue(actor1RoomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));

        // Connect actor 2
        actor2LocalMedia = LocalMedia.create(context);
        token = AccessTokenHelper.obtainCapabilityToken(TEST_USER2);
        actor2AccessManager = new AccessManager(context, token, null);
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                actor2VideoClient = new VideoClient(context, actor2AccessManager);
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
        actor1AccessManager.dispose();
        actor1AccessManager = null;
        actor2AccessManager.dispose();
        actor2AccessManager = null;
        fakeVideoCapturer = null;
    }

    // Audio
    @Test
    public void media_onAudioTrackAdded() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onAudioTrackRemoved() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackRemovedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        actor2LocalMedia.removeAudioTrack(audioTrack);
        assertTrue(mediaListener.onAudioTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onAudioTrackEnabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackEnabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(false);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        audioTrack.enable(true);
        assertTrue(mediaListener.onAudioTrackEnabledLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onAudioTrackDisabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        audioTrack.enable(false);
        assertTrue(mediaListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));
    }

    // Video
    @Test
    public void media_onVideoTrackAdded() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onVideoTrackRemoved() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        actor2LocalMedia.removeVideoTrack(videoTrack);
        assertTrue(mediaListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(true);
    }

    @Test
    public void media_shouldSupportAddNullRenderer() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<VideoTrack> videoTracks = participant.getMedia().getVideoTracks();
        assertEquals(1, videoTracks.size());
        videoTracks.get(0).addRenderer(null);
    }

    @Test
    public void media_shouldAddRemoveRenderer() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<VideoTrack> videoTracks = participant.getMedia().getVideoTracks();
        assertEquals(1, videoTracks.size());
        FakeVideoRenderer renderer = new FakeVideoRenderer();
        videoTracks.get(0).addRenderer(renderer);
        assertEquals(1, videoTracks.get(0).getRenderers().size());
        videoTracks.get(0).removeRenderer(renderer);
        assertEquals(0, videoTracks.get(0).getRenderers().size());
    }

    @Test
    public void media_shouldFailToAddRendererOnRemovedTrack() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack localVideoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        List<VideoTrack> videoTracks = participant.getMedia().getVideoTracks();
        assertEquals(1, videoTracks.size());
        FakeVideoRenderer renderer = new FakeVideoRenderer();
        VideoTrack videoTrack = videoTracks.get(0);

        actor1RoomListener.onParticipantDisconnectedLatch = new CountDownLatch(1);
        actor2Room.disconnect();
        assertTrue(actor1RoomListener.onParticipantDisconnectedLatch.await(20, TimeUnit.SECONDS));

        videoTrack.addRenderer(renderer);
    }

    @Test
    public void media_onVideoTrackEnabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(false, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        videoTrack.enable(true);
        assertTrue(mediaListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void media_onVideoTrackDisabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onVideoTrackDisabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
        videoTrack.enable(false);
        assertTrue(mediaListener.onVideoTrackDisabledLatch.await(20, TimeUnit.SECONDS));
    }

}
