package com.twilio.video;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class LocalAudioTrackTest {
    @Parameterized.Parameters(name = "enabled: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false},
                {true}});
    }

    private final boolean enabled;
    private Context context;
    private LocalMedia localMedia;
    private LocalAudioTrack localAudioTrack;


    public LocalAudioTrackTest(boolean enabled) {
        this.enabled = enabled;
    }

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        localMedia = LocalMedia.create(context);
        localAudioTrack = localMedia.addAudioTrack(enabled);
    }

    @After
    public void teardown() {
        localMedia.removeAudioTrack(localAudioTrack);
        localMedia.release();
    }

    @Test
    public void isEnabled_shouldReflectConstructedState() {
        assertEquals(enabled, localAudioTrack.isEnabled());
    }

    @Test
    public void isEnabled_shouldReturnFalseAfterRemoved() {
        assertEquals(enabled, localAudioTrack.isEnabled());
        assertTrue(localMedia.removeAudioTrack(localAudioTrack));
        assertFalse(localAudioTrack.isEnabled());
    }

    @Test
    public void enable_shouldChangeAudioTrackState() {
        boolean updatedEnabled = !enabled;

        localAudioTrack.enable(updatedEnabled);

        assertEquals(updatedEnabled, localAudioTrack.isEnabled());
    }

    @Test
    public void enable_shouldAllowToggling() {
        boolean enabled = this.enabled;
        int numIterations = 10;

        for (int i = 0 ; i < numIterations ; i++) {
            boolean updatedEnabled = !enabled;

            localAudioTrack.enable(updatedEnabled);

            assertEquals(updatedEnabled, localAudioTrack.isEnabled());
            enabled = updatedEnabled;
        }
    }

    @Test
    public void enable_shouldAllowSameState() {
        int numIterations = 10;

        for (int i = 0 ; i < numIterations ; i++) {
            localAudioTrack.enable(enabled);

            assertEquals(enabled, localAudioTrack.isEnabled());
        }
    }

    @Test
    public void enable_shouldNotBeAllowedAfterRemoved() {
        boolean updatedEnabled = !enabled;

        localMedia.removeAudioTrack(localAudioTrack);
        localAudioTrack.enable(updatedEnabled);

        assertEquals(false, localAudioTrack.isEnabled());
    }
}
