package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseMediaTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.FakeVideoRenderer;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class VideoTrackTest extends BaseMediaTest {
    @Test
    public void addRenderer_shouldNotCrashForNullRenderer() throws InterruptedException {
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
    public void canAddAndRemoveRenderer() throws InterruptedException {
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
    public void shouldFailToAddRendererOnRemovedTrack() throws InterruptedException {
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
}
