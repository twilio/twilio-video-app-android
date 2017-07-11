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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class LocalParticipantUnitTest {
    private static final int INT_MAX = 25;

    private final Random random = new Random();
    private LocalParticipant localParticipant;
    @Mock LocalAudioTrack mockAudioTrack;
    @Mock LocalVideoTrack mockVideoTrackOne;
    @Mock LocalVideoTrack mockVideoTrackTwo;

    @Before
    public void setup() {
        localParticipant = new LocalParticipant(random.nextLong(),
                String.valueOf(random.nextInt(INT_MAX)),
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockAudioTrack),
                Arrays.asList(mockVideoTrackOne, mockVideoTrackTwo));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        localParticipant.getAudioTracks().add(mockAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        localParticipant.getVideoTracks().add(mockVideoTrackOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedAudioTracks() {
        localParticipant.getPublishedAudioTracks().add(mockAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedVideoTracks() {
        localParticipant.getPublishedVideoTracks().add(mockVideoTrackOne);
    }
}
