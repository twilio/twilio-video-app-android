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
import static junit.framework.Assert.assertTrue;

@RunWith(Parameterized.class)
@LargeTest
public class LocalVideoTrackEnabledParameterizedTest extends BaseLocalVideoTrackTest {
    @Parameterized.Parameters(name = "enabled: {0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {false},
                {true}});
    }

    private final boolean enabled;

    public LocalVideoTrackEnabledParameterizedTest(boolean enabled) {
        this.enabled = enabled;
    }

    @Before
    public void setup() {
        super.setup();
        localVideoTrack = LocalVideoTrack.create(context, enabled, fakeVideoCapturer);
    }

    @After
    public void teardown() {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void isEnabled_shouldReflectConstructedState() {
        assertEquals(enabled, localVideoTrack.isEnabled());
    }

    @Test
    public void isEnabled_shouldReturnFalseAfterReleased() {
        assertEquals(enabled, localVideoTrack.isEnabled());
        localVideoTrack.release();
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
    public void enable_shouldNotBeAllowedAfterReleased() {
        boolean updatedEnabled = !enabled;

        localVideoTrack.release();
        localVideoTrack.enable(updatedEnabled);

        assertEquals(false, localVideoTrack.isEnabled());
    }
}
