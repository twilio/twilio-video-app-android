package com.twilio.video;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.ui.MediaTestActivity;
import com.twilio.video.util.PermissionUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
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
    private LocalMedia localMedia;
    private LocalAudioTrack localAudioTrack;

    @Before
    public void setup() {
        mediaTestActivity = activityRule.getActivity();
        PermissionUtils.allowPermissions(mediaTestActivity);
        localMedia = LocalMedia.create(mediaTestActivity);
    }

    @After
    public void teardown() {
        localMedia.release();
    }

    @Test
    public void canAddAudioTrackWithOptions() {
        List<AudioOptions> audioOptionsList = getAllAudioOptionsPermutations();

        for (AudioOptions audioOptions : audioOptionsList) {
            localAudioTrack = localMedia.addAudioTrack(true, audioOptions);

            // Validate the audio track was added
            assertNotNull("Failed to add AudioTrack with options: " + audioOptions,
                    localAudioTrack);
            assertTrue("AudioTrack added is not enabled with options: " + audioOptions,
                    localAudioTrack.isEnabled());

            // Remove the audio track and continue to next audio options configuration
            localMedia.removeAudioTrack(localAudioTrack);
        }
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
}
