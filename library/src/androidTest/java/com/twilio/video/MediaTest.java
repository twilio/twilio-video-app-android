package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseMediaTest;
import com.twilio.video.helper.CallbackHelper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MediaTest extends BaseMediaTest {
    @Test
    public void shouldReceiveOnAudioTrackAdded() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReceiveOnAudioTrackRemoved() throws InterruptedException {
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
    public void shouldReceiveOnAudioTrackEnabled() throws InterruptedException {
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
    public void shouldReceiveOnAudioTrackDisabled() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onAudioTrackAddedLatch = new CountDownLatch(1);
        mediaListener.onAudioTrackDisabledLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        LocalAudioTrack audioTrack = actor2LocalMedia.addAudioTrack(true);
        assertTrue(mediaListener.onAudioTrackAddedLatch.await(20, TimeUnit.SECONDS));
        audioTrack.enable(false);
        assertTrue(mediaListener.onAudioTrackDisabledLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReceiveOnVideoTrackAdded() throws InterruptedException {
        CallbackHelper.FakeMediaListener mediaListener = new CallbackHelper.FakeMediaListener();
        mediaListener.onVideoTrackAddedLatch = new CountDownLatch(1);
        participant.getMedia().setListener(mediaListener);

        actor2LocalMedia.addVideoTrack(true, fakeVideoCapturer);
        assertTrue(mediaListener.onVideoTrackAddedLatch.await(20, TimeUnit.SECONDS));
    }

    @Test
    public void shouldReceiveOnVideoTrackRemoved() throws InterruptedException {
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
    public void shouldReceiveOnVideoTrackEnabled() throws InterruptedException {
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
    public void shouldReceiveOnVideoTrackDisabled() throws InterruptedException {
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
