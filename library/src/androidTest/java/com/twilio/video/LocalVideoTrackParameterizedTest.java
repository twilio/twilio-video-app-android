package com.twilio.video;

import android.support.test.filters.LargeTest;

import com.twilio.video.base.BaseLocalVideoTrackTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class LocalVideoTrackParameterizedTest extends BaseLocalVideoTrackTest {
    @Parameterized.Parameters(name = "enabled: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false},
                {true}});
    }

    private final boolean enabled;
    private LocalVideoTrack localVideoTrack;

    public LocalVideoTrackParameterizedTest(boolean enabled) {
        this.enabled = enabled;
    }

    @Before
    public void setup() {
        super.setup();
        localVideoTrack = localMedia.addVideoTrack(enabled, fakeVideoCapturer);
    }

    @After
    public void teardown() {
        localMedia.removeVideoTrack(localVideoTrack);
        super.teardown();
    }

    @Test
    public void isEnabled_shouldReflectConstructedState() {
        assertEquals(enabled, localVideoTrack.isEnabled());
    }

    @Test
    public void isEnabled_shouldReturnFalseAfterRemoved() {
        assertEquals(enabled, localVideoTrack.isEnabled());
        assertTrue(localMedia.removeVideoTrack(localVideoTrack));
        assertFalse(localVideoTrack.isEnabled());
    }

    @Test
    public void enable_shouldChangeAudioTrackState() {
        boolean updatedEnabled = !enabled;

        localVideoTrack.enable(updatedEnabled);

        assertEquals(updatedEnabled, localVideoTrack.isEnabled());
    }

    @Test
    public void enable_shouldAllowToggling() {
        boolean enabled = this.enabled;
        int numIterations = 10;

        for (int i = 0 ; i < numIterations ; i++) {
            boolean updatedEnabled = !enabled;

            localVideoTrack.enable(updatedEnabled);

            assertEquals(updatedEnabled, localVideoTrack.isEnabled());
            enabled = updatedEnabled;
        }
    }

    @Test
    public void enable_shouldAllowSameState() {
        int numIterations = 10;

        for (int i = 0 ; i < numIterations ; i++) {
            localVideoTrack.enable(enabled);

            assertEquals(enabled, localVideoTrack.isEnabled());
        }
    }

    @Test
    public void enable_shouldNotBeAllowedAfterRemoved() {
        boolean updatedEnabled = !enabled;

        localMedia.removeVideoTrack(localVideoTrack);
        localVideoTrack.enable(updatedEnabled);

        assertEquals(false, localVideoTrack.isEnabled());
    }
}
