package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseMediaTest;
import com.twilio.video.helper.CallbackHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MediaTest extends BaseMediaTest {
    @Test
    public void shouldReceiveTrackEvents() throws InterruptedException {
        // Audio track added
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);
        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Audio track disabled
        mediaListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        audioTrack.enable(false);
        assertTrue(mediaListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track enabled
        mediaListener.onAudioTrackEnabledLatch = new CountDownLatch(1);
        audioTrack.enable(true);
        assertTrue(mediaListener.onAudioTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Audio track removed
        mediaListener.onAudioTrackRemovedLatch = new CountDownLatch(1);
        actor2LocalMedia.removeAudioTrack(audioTrack);
        assertTrue(mediaListener.onAudioTrackRemovedLatch.await(20, TimeUnit.SECONDS));

        // Video track added
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        LocalVideoTrack videoTrack = actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));

        // Video track disabled
        mediaListener.onVideoTrackDisabledLatch = new CountDownLatch(1);
        videoTrack.enable(false);
        assertTrue(mediaListener.onVideoTrackDisabledLatch.await(20, TimeUnit.SECONDS));

        // Video track enabled
        mediaListener.onVideoTrackEnabledLatch = new CountDownLatch(1);
        videoTrack.enable(true);
        assertTrue(mediaListener.onVideoTrackEnabledLatch.await(20, TimeUnit.SECONDS));

        // Video track removed
        mediaListener.onVideoTrackRemovedLatch = new CountDownLatch(1);
        actor2LocalMedia.removeVideoTrack(videoTrack);
        assertTrue(mediaListener.onVideoTrackRemovedLatch.await(20, TimeUnit.SECONDS));
    }
}
