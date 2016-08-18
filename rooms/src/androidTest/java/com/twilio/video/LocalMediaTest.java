package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalMediaTest {
    private Context context;
    private MediaFactory mediaFactory;
    private LocalMedia localMedia;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mediaFactory = MediaFactory.instance(context);
        localMedia = mediaFactory.createLocalMedia(null);
    }

    @After
    public void teardown() {
        localMedia.release();
        mediaFactory.release();
    }

    @Test
    public void release_shouldBeIdempotent() {
        localMedia.release();
        localMedia.release();
    }

    @Test
    public void canAddAudioTrack() throws InterruptedException {
        final CountDownLatch audioTrackAdded = new CountDownLatch(1);
        localMedia.setLocalMediaListener(new LocalMedia.Listener() {
            @Override
            public void onLocalAudioTrackAdded(AudioTrack localAudioTrack) {
                audioTrackAdded.countDown();

            }

            @Override
            public void onLocalAudioTrackError() {

            }

            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia,
                                               LocalVideoTrack videoTrack) {
                fail("Expected audio track added event but received video track added");
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia,
                                                 LocalVideoTrack videoTrack) {
                fail("Expected audio track added event but received video track removed");

            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia,
                                               LocalVideoTrack track,
                                               VideoException exception) {
                fail("Expected audio track added event but received video track error");
            }
        });
        localMedia.addAudioTrack();
        assertTrue(audioTrackAdded.await(5, TimeUnit.SECONDS));
    }

    // FIXME we should support multiple audio tracks
    @Test
    @Ignore
    public void canAddMultipleAudioTracks() throws InterruptedException {
        int numAudioTracks = 5;
        final CountDownLatch audioTracksAdded = new CountDownLatch(numAudioTracks);
        localMedia.setLocalMediaListener(new LocalMedia.Listener() {
            @Override
            public void onLocalAudioTrackAdded(AudioTrack localAudioTrack) {
                audioTracksAdded.countDown();

            }

            @Override
            public void onLocalAudioTrackError() {
                fail("Expected audio track added event but received audio track error");
            }

            @Override
            public void onLocalVideoTrackAdded(LocalMedia localMedia,
                                               LocalVideoTrack videoTrack) {
                fail("Expected audio track added event but received video track added");
            }

            @Override
            public void onLocalVideoTrackRemoved(LocalMedia localMedia,
                                                 LocalVideoTrack videoTrack) {
                fail("Expected audio track added event but received video track removed");
            }

            @Override
            public void onLocalVideoTrackError(LocalMedia localMedia,
                                               LocalVideoTrack track,
                                               VideoException exception) {
                fail("Expected audio track added event but received video track error");
            }
        });

        for (int i = 0 ; i < numAudioTracks ; i++) {
            localMedia.addAudioTrack();
        }

        assertTrue(audioTracksAdded.await(5, TimeUnit.SECONDS));
    }

    @Test(expected = IllegalStateException.class)
    public void addAudioTrack_shouldFailAfterRelease() {
        localMedia.release();
        localMedia.addAudioTrack();
    }

    @Test
    public void canAddVideoTrack() {
        // TODO
    }

    @Test
    public void canAddAudioAndVideoTrack() {
        // TODO
    }



    @Test
    public void canAddMultipleVideoTracks() {
        // TODO
    }

    @Test
    public void canAddMultipleAudioAndVideoTracks() {
        // TODO
    }
}
