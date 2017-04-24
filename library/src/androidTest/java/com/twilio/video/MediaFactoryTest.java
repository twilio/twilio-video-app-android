package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.util.FakeVideoCapturer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MediaFactoryTest {
    private static final int NUM_TRACKS = 10;

    private final Random random = new Random();
    private Context context;
    private MediaFactory mediaFactory;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mediaFactory = MediaFactory.instance(context);
    }

    @After
    public void teardown() {
        mediaFactory.release();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canCreateAudioTrack() {
        LocalAudioTrack localAudioTrack = mediaFactory.createAudioTrack(true, null);

        assertNotNull(localAudioTrack);
        localAudioTrack.release();
    }

    @Test
    public void canCreateVideoTrack() {
        LocalVideoTrack localVideoTrack = mediaFactory.createVideoTrack(true,
                new FakeVideoCapturer(), LocalVideoTrack.defaultVideoConstraints);

        assertNotNull(localVideoTrack);
        localVideoTrack.release();
    }

    @Test
    public void canCreateAudioAndVideoTracks() {
        LocalAudioTrack localAudioTrack = mediaFactory.createAudioTrack(true, null);
        LocalVideoTrack localVideoTrack = mediaFactory.createVideoTrack(true,
                new FakeVideoCapturer(), LocalVideoTrack.defaultVideoConstraints);
        assertNotNull(localAudioTrack);
        assertNotNull(localVideoTrack);
        localAudioTrack.release();
        localVideoTrack.release();
    }

    @Test
    public void release_shouldBeIdempotent() {
        mediaFactory.release();
        mediaFactory.release();
    }

    @Test(expected = IllegalStateException.class)
    public void createAudioTrack_shouldFailAfterAllTracksHaveBeenReleased() {
        Track[] tracks = new Track[NUM_TRACKS];

        // Create random tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            tracks[i] = random.nextBoolean() ?
                    (mediaFactory.createAudioTrack(true, null)) :
                    (mediaFactory.createVideoTrack(true, new FakeVideoCapturer(),
                            LocalVideoTrack.defaultVideoConstraints));
        }

        // Destroy all tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            if (tracks[i].getClass() == LocalAudioTrack.class) {
                LocalAudioTrack localAudioTrack = (LocalAudioTrack) tracks[i];
                localAudioTrack.release();
            } else if (tracks[i].getClass() == LocalVideoTrack.class) {
                LocalVideoTrack localVideoTrack = (LocalVideoTrack) tracks[i];
                localVideoTrack.release();
            } else {
                throw new RuntimeException("Created unexpected track instance");
            }
        }

        // With all tracks released this should raise exception
        mediaFactory.createAudioTrack(true, null);
    }

    @Test(expected = IllegalStateException.class)
    public void createVideoTrack_shouldFailAfterAllTracksHaveBeenReleased() {
        Track[] tracks = new Track[NUM_TRACKS];

        // Create random tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            tracks[i] = random.nextBoolean() ?
                    (mediaFactory.createAudioTrack(true, null)) :
                    (mediaFactory.createVideoTrack(true, new FakeVideoCapturer(),
                            LocalVideoTrack.defaultVideoConstraints));
        }

        // Destroy all tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            if (tracks[i].getClass() == LocalAudioTrack.class) {
                LocalAudioTrack localAudioTrack = (LocalAudioTrack) tracks[i];
                localAudioTrack.release();
            } else if (tracks[i].getClass() == LocalVideoTrack.class) {
                LocalVideoTrack localVideoTrack = (LocalVideoTrack) tracks[i];
                localVideoTrack.release();
            } else {
                throw new RuntimeException("Created unexpected track instance");
            }
        }

        // With all tracks released this should raise exception
        mediaFactory.createVideoTrack(true, new FakeVideoCapturer(),
                LocalVideoTrack.defaultVideoConstraints);
    }
}
