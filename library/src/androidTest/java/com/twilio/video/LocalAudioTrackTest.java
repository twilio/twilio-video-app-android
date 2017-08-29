/*
 * Copyright (C) 2017 Twilio, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twilio.video;

import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;

import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.PermissionUtils;
import com.twilio.video.util.RandUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
@SmallTest
public class LocalAudioTrackTest {
    private static final int NUM_AUDIO_OPTIONS = 7;
    private static final int NUM_AUDIO_OPTIONS_PERMUTATIONS = (int) Math.pow(2, NUM_AUDIO_OPTIONS);
    private static final int ECHO_CANCELLATION_INDEX = 0;
    private static final int AUTO_GAIN_CONTROL_INDEX = 1;
    private static final int NOISE_SUPRRESSION_INDEX = 2;
    private static final int HIGHPASS_FILTER_INDEX = 3;
    private static final int STEREO_SWAPPING_INDEX = 4;
    private static final int AUDIO_JITTER_BUFFER_FAST_ACCELERATE_INDEX = 5;
    private static final int TYPING_DETECTION_INDEX = 6;

    @Rule public ActivityTestRule<MediaTestActivity> activityRule =
            new ActivityTestRule<>(MediaTestActivity.class);
    private MediaTestActivity mediaTestActivity;
    private LocalAudioTrack localAudioTrack;

    @Before
    public void setup() {
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
    }

    @After
    public void teardown() {
        if (localAudioTrack != null) {
            localAudioTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    @Parameters({ "false", "true" })
    @TestCaseName("{method}[enabled: {0}]")
    public void canCreateAudioTrack(boolean enabled) {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, enabled);

        assertNotNull(localAudioTrack);
        assertEquals(enabled, localAudioTrack.isEnabled());
    }

    @Test
    @Parameters
    @TestCaseName("{method}[name: {0}]")
    public void canCreateAudioTrackWithName(String name, String expectedName) {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true, name);

        assertNotNull(localAudioTrack);
        assertEquals(expectedName, localAudioTrack.getName());
    }

    @Test
    public void canCreateMultipleAudioTracks() {
        int numAudioTracks = 5;
        boolean[] expectedEnabled = new boolean[]{ false, true, true, false, false };

        for (int i = 0 ; i < numAudioTracks ; i++) {
            LocalAudioTrack localAudioTrack = LocalAudioTrack.create(mediaTestActivity,
                    expectedEnabled[i]);

            Assert.assertNotNull(localAudioTrack);
            assertEquals(expectedEnabled[i], localAudioTrack.isEnabled());
            localAudioTrack.release();
        }
    }

    @Test
    public void canReleaseAudioTrack() {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);

        assertNotNull(localAudioTrack);
        localAudioTrack.release();
    }

    @Test
    public void release_shouldBeIdempotent() {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);

        assertNotNull(localAudioTrack);
        localAudioTrack.release();
        localAudioTrack.release();
    }

    @Test
    public void canCreateAudioTrackWithOptions() {
        List<AudioOptions> audioOptionsList = getAllAudioOptionsPermutations();

        for (AudioOptions audioOptions : audioOptionsList) {
            localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true, audioOptions);

            // Validate the audio track was created
            assertNotNull("Failed to create RemoteAudioTrack with options: " + audioOptions,
                    localAudioTrack);
            assertTrue("RemoteAudioTrack created is not enabled with options: " + audioOptions,
                    localAudioTrack.isEnabled());

            // Remove the audio track and continue to next audio options configuration
            localAudioTrack.release();
        }
    }

    @Test
    public void enable_shouldNotBeAllowedAfterReleased() {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, false);
        assertTrue(true);
        localAudioTrack.release();
        localAudioTrack.enable(true);
        assertFalse(localAudioTrack.isEnabled());
    }

    @Test
    public void enable_shouldChangeState() {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localAudioTrack.enable(false);

        assertFalse(localAudioTrack.isEnabled());
    }

    @Test
    public void isEnabled_shouldReturnFalseAfterReleased() {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        assertTrue(localAudioTrack.isEnabled());
        localAudioTrack.release();
        assertFalse(localAudioTrack.isEnabled());
    }

    @Test
    public void enable_shouldAllowSameState() {
        localAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        localAudioTrack.enable(true);
        localAudioTrack.enable(true);

        assertTrue(localAudioTrack.isEnabled());
    }

    /*
     * Returns all possible AudioOptions configurations in a list.
     */
    private List<AudioOptions> getAllAudioOptionsPermutations() {
        boolean[][] audioOptionsMatrix = getAudioOptionsPermutationMatrix();
        List<AudioOptions> audioOptionsList = new ArrayList<>(NUM_AUDIO_OPTIONS_PERMUTATIONS);

        for (int i = 0 ; i < NUM_AUDIO_OPTIONS_PERMUTATIONS ; i++) {
            AudioOptions audioOptions = new AudioOptions.Builder()
                    .echoCancellation(audioOptionsMatrix[i][ECHO_CANCELLATION_INDEX])
                    .autoGainControl(audioOptionsMatrix[i][AUTO_GAIN_CONTROL_INDEX])
                    .noiseSuppression(audioOptionsMatrix[i][NOISE_SUPRRESSION_INDEX])
                    .highpassFilter(audioOptionsMatrix[i][HIGHPASS_FILTER_INDEX])
                    .stereoSwapping(audioOptionsMatrix[i][STEREO_SWAPPING_INDEX])
                    .audioJitterBufferFastAccelerate(audioOptionsMatrix[i]
                            [AUDIO_JITTER_BUFFER_FAST_ACCELERATE_INDEX])
                    .typingDetection(audioOptionsMatrix[i][TYPING_DETECTION_INDEX])
                    .build();

            audioOptionsList.add(i, audioOptions);
        }

        return audioOptionsList;
    }

    /*
     * Builds a matrix of AudioOptions permutations.
     */
    private boolean[][] getAudioOptionsPermutationMatrix() {
        boolean[][] audioOptionsMatrix =
                new boolean[NUM_AUDIO_OPTIONS_PERMUTATIONS][NUM_AUDIO_OPTIONS];

        for (int i = 0x0 ; i < NUM_AUDIO_OPTIONS_PERMUTATIONS ; i++) {
            boolean[] audioOptions = new boolean[NUM_AUDIO_OPTIONS];
            for (int j = 0 ; j < NUM_AUDIO_OPTIONS ; j++) {
                boolean enableAudioOption = (i & (0x1 << j)) == 0;
                audioOptions[j] = enableAudioOption;
            }
            audioOptionsMatrix[i] = audioOptions;
        }

        return audioOptionsMatrix;
    }

    private Object[] parametersForCanCreateAudioTrackWithName() {
        String audioTrackName = RandUtils.generateRandomString(10);

        return new Object[]{
                new Object[]{null, ""},
                new Object[]{"", ""},
                new Object[]{audioTrackName, audioTrackName}
        };
    }
}
