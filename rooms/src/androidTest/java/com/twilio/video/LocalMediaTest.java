package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
