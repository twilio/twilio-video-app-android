package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class LocalAudioTrackEnabledParameterizedTest {
    @Parameterized.Parameters(name = "enabled: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false},
                {true}});
    }

    private final boolean enabled;
    @Rule public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private LocalMedia localMedia;
    private LocalAudioTrack localAudioTrack;


    public LocalAudioTrackEnabledParameterizedTest(boolean enabled) {
        this.enabled = enabled;
    }

    @Before
    public void setup() {
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        localMedia = LocalMedia.create(mediaTestActivity);
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
