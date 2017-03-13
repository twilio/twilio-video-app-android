package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.AccessTokenUtils;
import com.twilio.video.util.Constants;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
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
public class VideoClientTest extends BaseClientTest {
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String token;
    private String roomName;
    private LocalMedia localMedia;
    private CallbackHelper.FakeRoomListener roomListener;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomListener = new CallbackHelper.FakeRoomListener();
        PermissionUtils.allowPermissions(mediaTestActivity);
        token = AccessTokenUtils.getAccessToken(Constants.PARTICIPANT_ALICE);
        roomName = RandUtils.generateRandomString(20);
        localMedia = LocalMedia.create(mediaTestActivity);
        VideoClient.setLogLevel(LogLevel.ALL);
    }

    @After
    public void teardown() {
        localMedia.release();
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenContextIsNull() {
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .roomName(roomName)
            .build();
        Room room = VideoClient.connect(null, connectOptions, roomListener);
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenConnectOptionsIsNull() {
        Room room = VideoClient.connect(mediaTestActivity, null, roomListener);
    }

    @Test
    public void logLevel_shouldBeRetained() {
        VideoClient.setLogLevel(LogLevel.DEBUG);
        assertEquals(LogLevel.DEBUG, VideoClient.getLogLevel());
    }

    @Test
    public void getVersion_shouldReturnValidSemVerFormattedVersion() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = VideoClient.getVersion();

        assertNotNull(version);
        assertTrue(version.matches(semVerRegex));
    }

    @Test
    public void connect_shouldConnectToRoom() throws InterruptedException {
        roomListener.onConnectedLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .build();
        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertEquals(room.getSid(), room.getName());
        room.disconnect();
    }

    @Test(expected = NullPointerException.class)
    public void connect_shouldThrowExceptionWhenRoomListenerIsNull() {
        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
            .build();
        VideoClient.connect(mediaTestActivity, connectOptions, null);
    }

    @Test
    public void connect_shouldAllowLocalMedia() throws InterruptedException {
        CallbackHelper.FakeRoomListener roomListener = new CallbackHelper.FakeRoomListener();
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        room.disconnect();
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudio() throws InterruptedException {
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        room.disconnect();
    }

    @Test
    public void connect_shouldAllowLocalMediaWithVideo() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));
        room.disconnect();
    }

    @Test
    public void connect_shouldAllowLocalMediaWithAudioAndVideo() throws InterruptedException {
        FakeVideoCapturer fakeVideoCapturer = new FakeVideoCapturer();
        LocalAudioTrack localAudioTrack = localMedia.addAudioTrack(true);
        LocalVideoTrack localVideoTrack = localMedia.addVideoTrack(true, fakeVideoCapturer);
        roomListener.onConnectedLatch = new CountDownLatch(1);

        ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                .roomName(roomName)
                .localMedia(localMedia)
                .build();
        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectedLatch.await(20, TimeUnit.SECONDS));
        assertNotNull(room.getLocalParticipant().getLocalMedia());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));
        room.disconnect();
    }

    @Test
    public void connect_shouldFailToConnectWithBadToken() throws InterruptedException {
        roomListener.onConnectFailureLatch = new CountDownLatch(1);
        ConnectOptions connectOptions = new ConnectOptions.Builder("bad token")
            .roomName(roomName)
            .localMedia(localMedia)
            .build();
        Room room = VideoClient.connect(mediaTestActivity, connectOptions, roomListener);
        assertTrue(roomListener.onConnectFailureLatch.await(20, TimeUnit.SECONDS));
        assertEquals(roomListener.getTwilioException().getCode(),
            TwilioException.ACCESS_TOKEN_INVALID_EXCEPTION);
        assertNotNull(roomListener.getTwilioException().getMessage());
    }
}
