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
        LocalMedia localMedia = mediaFactory.createLocalMedia(null);

        localMedia.release();
        assertNotNull(localMedia);
    }

    @Test
    public void release_shouldBeIdempotent() {
        mediaFactory.release();
        mediaFactory.release();
    }

    @Test(expected = IllegalStateException.class)
    public void failsWhenCreatingLocalMediaAfterRelease() {
        mediaFactory.release();
        mediaFactory.createLocalMedia(null);
    }
}
