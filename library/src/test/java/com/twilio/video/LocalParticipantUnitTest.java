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

import android.os.Handler;

import com.twilio.video.util.Constants;

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
    @Mock Handler handler;
    @Mock LocalAudioTrackPublication mockLocalAudioTrackPublication;
    @Mock LocalVideoTrackPublication mockLocalVideoTrackPublicationOne;
    @Mock LocalDataTrackPublication mockLocalDataTrackPublication;

    @Before
    public void setup() {
        localParticipant = new LocalParticipant(random.nextLong(),
                Constants.MOCK_PARTICIPANT_SID,
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockLocalAudioTrackPublication),
                Arrays.asList(mockLocalVideoTrackPublicationOne),
                Arrays.asList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullSid() {
        new LocalParticipant(random.nextLong(),
                null,
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockLocalAudioTrackPublication),
                Arrays.asList(mockLocalVideoTrackPublicationOne),
                Arrays.asList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptySid() {
        new LocalParticipant(random.nextLong(),
                "",
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockLocalAudioTrackPublication),
                Arrays.asList(mockLocalVideoTrackPublicationOne),
                Arrays.asList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullIdentity() {
        new LocalParticipant(random.nextLong(),
                Constants.MOCK_PARTICIPANT_SID,
                null,
                Arrays.asList(mockLocalAudioTrackPublication),
                Arrays.asList(mockLocalVideoTrackPublicationOne),
                Arrays.asList(mockLocalDataTrackPublication),
                handler);
    }

    @Test
    public void shouldSucceedWithValidTrackSid() {
        new LocalParticipant(random.nextLong(),
                Constants.MOCK_PARTICIPANT_SID,
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockLocalAudioTrackPublication),
                Arrays.asList(mockLocalVideoTrackPublicationOne),
                Arrays.asList(mockLocalDataTrackPublication),
                handler);
    }

    @Test(expected = NullPointerException.class)
    public void setListener_shouldNotAllowNull() {
        localParticipant.setListener(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        localParticipant.getAudioTracks().add(mockLocalAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        localParticipant.getVideoTracks().add(mockLocalVideoTrackPublicationOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedAudioTracks() {
        localParticipant.getLocalAudioTracks().add(mockLocalAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingPublishedVideoTracks() {
        localParticipant.getLocalVideoTracks().add(mockLocalVideoTrackPublicationOne);
    }
}
