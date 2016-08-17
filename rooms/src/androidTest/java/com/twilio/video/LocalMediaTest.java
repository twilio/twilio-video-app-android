package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalMediaTest {
    LocalMedia localMedia;

    @Before
    public void setup() {
        localMedia = LocalMedia.create(null);
    }

    @After
    public void teardown() {
        localMedia.release();
    }

    @Test
    public void canAddAudioTrack() {
        // TODO
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
    public void canAddMultipleAudioTracks() {
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
