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
        mediaFactory = MediaFactory.instance(this, context);
    }

    @After
    public void teardown() {
        mediaFactory.release(this);
        assertTrue(MediaFactory.isReleased());
    }

    @Test(expected = NullPointerException.class)
    public void createAudioTrack_shouldNotAllowNullContext() {
        mediaFactory.createAudioTrack(null, true, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void createVideoTrack_shouldNotAllowNullContext() {
        mediaFactory.createVideoTrack(null,
                true,
                new FakeVideoCapturer(),
                LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS,
                null);
    }

    @Test(expected = NullPointerException.class)
    public void createDataTrack_shouldNotAllowNullContext() {
        mediaFactory.createDataTrack(null,
                true,
                DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                null);
    }

    @Test
    public void canCreateAudioTrack() {
        LocalAudioTrack localAudioTrack = mediaFactory.createAudioTrack(context,
                true,
                null,
                null);

        assertNotNull(localAudioTrack);
        localAudioTrack.release();
    }

    @Test
    public void canCreateVideoTrack() {
        LocalVideoTrack localVideoTrack = mediaFactory.createVideoTrack(context, true,
                new FakeVideoCapturer(), LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS, null);

        assertNotNull(localVideoTrack);
        localVideoTrack.release();
    }

    @Test
    public void canCreateDataTrack() {
        LocalDataTrack localDataTrack = mediaFactory.createDataTrack(context,
                true,
                DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                null);

        assertNotNull(localDataTrack);
        localDataTrack.release();
    }

    @Test
    public void canCreateMultipleTracks() {
        LocalAudioTrack localAudioTrack = mediaFactory.createAudioTrack(context,
                true,
                null,
                null);
        LocalVideoTrack localVideoTrack = mediaFactory.createVideoTrack(context,
                true,
                new FakeVideoCapturer(),
                LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS,
                null);
        LocalDataTrack localDataTrack = mediaFactory.createDataTrack(context,
                true,
                DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                null);
        assertNotNull(localAudioTrack);
        assertNotNull(localVideoTrack);
        assertNotNull(localDataTrack);
        localAudioTrack.release();
        localVideoTrack.release();
        localDataTrack.release();
    }

    @Test
    public void release_shouldBeIdempotent() {
        mediaFactory.release(this);
        mediaFactory.release(this);
    }

    @Test(expected = IllegalStateException.class)
    public void createAudioTrack_shouldFailAfterAllTracksHaveBeenReleased() {
        Track[] tracks = new Track[NUM_TRACKS];

        // Create random tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            int seed = Math.abs(random.nextInt()) % 3;

            switch (seed) {
                case 0:
                    tracks[i] = mediaFactory.createAudioTrack(context,
                            true,
                            null,
                            null);
                    break;
                case 1:
                    tracks[i] = mediaFactory.createVideoTrack(context,
                            true,
                            new FakeVideoCapturer(),
                            LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS,
                            null);
                    break;
                case 2:
                    tracks[i] = mediaFactory.createDataTrack(context,
                            true,
                            DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                            DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                            null);
                    break;

            }
        }

        // Destroy all tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            if (tracks[i].getClass() == LocalAudioTrack.class) {
                LocalAudioTrack localAudioTrack = (LocalAudioTrack) tracks[i];
                localAudioTrack.release();
            } else if (tracks[i].getClass() == LocalVideoTrack.class) {
                LocalVideoTrack localVideoTrack = (LocalVideoTrack) tracks[i];
                localVideoTrack.release();
            } else if (tracks[i].getClass() == LocalDataTrack.class) {
                LocalDataTrack localDataTrack = (LocalDataTrack) tracks[i];
                localDataTrack.release();
            } else {
                throw new RuntimeException("Created unexpected track instance");
            }
        }

        // Test itself is owner so release before trying to create audio track
        mediaFactory.release(this);

        // With all tracks and test owner released this should raise exception
        mediaFactory.createAudioTrack(context, true, null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void createVideoTrack_shouldFailAfterAllTracksHaveBeenReleased() {
        Track[] tracks = new Track[NUM_TRACKS];

        // Create random tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            int seed = Math.abs(random.nextInt()) % 3;

            switch (seed) {
                case 0:
                    tracks[i] = mediaFactory.createAudioTrack(context,
                            true,
                            null,
                            null);
                    break;
                case 1:
                    tracks[i] = mediaFactory.createVideoTrack(context,
                            true,
                            new FakeVideoCapturer(),
                            LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS,
                            null);
                    break;
                case 2:
                    tracks[i] = mediaFactory.createDataTrack(context,
                            true,
                            DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                            DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                            null);
                    break;

            }
        }

        // Destroy all tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            if (tracks[i].getClass() == LocalAudioTrack.class) {
                LocalAudioTrack localAudioTrack = (LocalAudioTrack) tracks[i];
                localAudioTrack.release();
            } else if (tracks[i].getClass() == LocalVideoTrack.class) {
                LocalVideoTrack localVideoTrack = (LocalVideoTrack) tracks[i];
                localVideoTrack.release();
            } else if (tracks[i].getClass() == LocalDataTrack.class) {
                LocalDataTrack localDataTrack = (LocalDataTrack) tracks[i];
                localDataTrack.release();
            } else {
                throw new RuntimeException("Created unexpected track instance");
            }
        }

        // Test itself is owner so release before trying to create video track
        mediaFactory.release(this);

        // With all tracks and test itself released this should raise exception
        mediaFactory.createVideoTrack(context,
                true,
                new FakeVideoCapturer(),
                LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS,
                null);
    }

    @Test(expected = IllegalStateException.class)
    public void createDataTrack_shouldFailAfterAllTracksHaveBeenReleased() {
        Track[] tracks = new Track[NUM_TRACKS];

        // Create random tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            int seed = Math.abs(random.nextInt()) % 3;

            switch (seed) {
                case 0:
                    tracks[i] = mediaFactory.createAudioTrack(context,
                            true,
                            null,
                            null);
                    break;
                case 1:
                    tracks[i] = mediaFactory.createVideoTrack(context,
                            true,
                            new FakeVideoCapturer(),
                            LocalVideoTrack.DEFAULT_VIDEO_CONSTRAINTS,
                            null);
                    break;
                case 2:
                    tracks[i] = mediaFactory.createDataTrack(context,
                            true,
                            DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                            DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                            null);
                    break;

            }
        }

        // Destroy all tracks
        for (int i = 0 ; i < NUM_TRACKS ; i++) {
            if (tracks[i].getClass() == LocalAudioTrack.class) {
                LocalAudioTrack localAudioTrack = (LocalAudioTrack) tracks[i];
                localAudioTrack.release();
            } else if (tracks[i].getClass() == LocalVideoTrack.class) {
                LocalVideoTrack localVideoTrack = (LocalVideoTrack) tracks[i];
                localVideoTrack.release();
            } else if (tracks[i].getClass() == LocalDataTrack.class) {
                LocalDataTrack localDataTrack = (LocalDataTrack) tracks[i];
                localDataTrack.release();
            } else {
                throw new RuntimeException("Created unexpected track instance");
            }
        }

        // Test itself is owner so release before trying to create data track
        mediaFactory.release(this);

        // With all tracks and test itself released this should raise exception
        mediaFactory.createDataTrack(context,
                true,
                DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME,
                DataTrackOptions.DEFAULT_MAX_RETRANSMITS,
                null);
    }

    @Test
    public void canCreateAndReleaseRepeatedly() {
        int numIterations = 100;
        for (int i = 0 ; i < numIterations ; i++) {
            MediaFactory mediaFactory = MediaFactory.instance(this, context);
            mediaFactory.release(this);
        }
    }
}
