package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertNotNull;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MediaFactoryTest {
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
    }

    @Test
    public void canCreateLocalMedia() {
        LocalMedia localMedia = mediaFactory.createLocalMedia();

        localMedia.release();
        assertNotNull(localMedia);
    }

    @Test
    public void release_shouldBeIdempotent() {
        mediaFactory.release();
        mediaFactory.release();
    }

    @Test(expected = IllegalStateException.class)
    public void createLocalMedia_shouldFailAfterAllLocalMediasHaveBeenReleased() {
        int numLocalMedias = 10;
        LocalMedia[] localMedias = new LocalMedia[numLocalMedias];

        // Create local medias
        for (int i = 0 ; i < numLocalMedias ; i++) {
            localMedias[i] = mediaFactory.createLocalMedia();
        }

        // Destroy local medias
        for (int i = 0 ; i < numLocalMedias ; i++) {
            localMedias[i].release();
        }

        // With all local medias released this should raise exception
        mediaFactory.createLocalMedia();
    }
}
