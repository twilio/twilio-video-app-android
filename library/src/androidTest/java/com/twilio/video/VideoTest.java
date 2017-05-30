package com.twilio.video;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseClientTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.twilioapi.VideoApiUtils;
import com.twilio.video.twilioapi.model.VideoRoom;
import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.Constants;
import com.twilio.video.util.CredentialsUtils;
import com.twilio.video.util.FakeVideoCapturer;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;
import com.twilio.video.util.RoomUtils;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class VideoTest extends BaseClientTest {
    @Rule
    public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private String token;
    private String roomName;
    private LocalAudioTrack localAudioTrack;
    private LocalVideoTrack localVideoTrack;
    private CallbackHelper.FakeRoomListener roomListener;

    @Before
    public void setup() throws InterruptedException {
        super.setup();
        mediaTestActivity = activityRule.getActivity();
        roomListener = new CallbackHelper.FakeRoomListener();
        PermissionUtils.allowPermissions(mediaTestActivity);
        roomName = RandUtils.generateRandomString(20);
        assertNotNull(RoomUtils.createRoom(roomName, Topology.P2P));
        token = CredentialsUtils.getAccessToken(Constants.PARTICIPANT_ALICE, Topology.P2P);
        Video.setLogLevel(LogLevel.ALL);
    }

    @After
    public void teardown() {
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        if (localVideoTrack != null) {
            localVideoTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void logLevel_shouldBeRetained() {
        Video.setLogLevel(LogLevel.DEBUG);
        assertEquals(LogLevel.DEBUG, Video.getLogLevel());
    }

    @Test
    public void getVersion_shouldReturnValidSemVerFormattedVersion() {
        String semVerRegex = "^([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-([0-9A-" +
                "Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+)?$";
        String version = Video.getVersion();

        assertNotNull(version);
        assertTrue(version.matches(semVerRegex));
    }

    @Test(expected = IllegalStateException.class)
    public void connectOptions_shouldNotAllowReleasedLocalAudioTrack() throws InterruptedException {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localAudioTrack.release();
        List<LocalAudioTrack> localAudioTracks = Collections.singletonList(localAudioTrack);
        new ConnectOptions.Builder(token)
                .roomName(roomName)
                .audioTracks(localAudioTracks)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void connectOptions_shouldNotAllowReleasedLocalVideoTrack() throws InterruptedException {
        localVideoTrack = LocalVideoTrack.create(mediaTestActivity, true, new FakeVideoCapturer());
        localVideoTrack.release();
        List<LocalVideoTrack> localVideoTracks = Collections.singletonList(localVideoTrack);
        new ConnectOptions.Builder(token)
                .roomName(roomName)
                .videoTracks(localVideoTracks)
                .build();
    }

    @Test
    @Ignore("Disconnecting while connecting results in native crash. See GSDK-1153")
    public void canConnectAndDisconnectRepeatedly() throws InterruptedException {
        int numIterations = 100;
        for (int i = 0 ; i < numIterations ; i++) {
            roomListener.onDisconnectedLatch = new CountDownLatch(1);
            ConnectOptions connectOptions = new ConnectOptions.Builder(token)
                    .build();
            Room room = Video.connect(mediaTestActivity, connectOptions, roomListener);
            room.disconnect();
            assertTrue(roomListener.onDisconnectedLatch.await(20, TimeUnit.SECONDS));
        }
    }
}
