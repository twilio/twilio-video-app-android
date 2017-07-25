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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class RemoteParticipantUnitTest {
    private static final int INT_MAX = 25;

    private final Random random = new Random();
    private RemoteParticipant remoteParticipant;
    @Mock RemoteAudioTrack mockRemoteAudioTrack;
    @Mock RemoteVideoTrack mockRemoteVideoTrackOne;
    @Mock RemoteVideoTrack mockRemoteVideoTrackTwo;
    @Mock Handler handler;

    @Before
    public void setup() {
        remoteParticipant = new RemoteParticipant(String.valueOf(random.nextInt(INT_MAX)),
                String.valueOf(random.nextInt(INT_MAX)),
                Arrays.asList(mockRemoteAudioTrack),
                Arrays.asList(mockRemoteVideoTrackOne, mockRemoteVideoTrackTwo),
                handler,
                random.nextLong());
    }

    @Test(expected = NullPointerException.class)
    public void setListener_shouldNotAllowNull() throws Exception {
        remoteParticipant.setListener(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        remoteParticipant.getAudioTracks().add(mockRemoteAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        remoteParticipant.getVideoTracks().add(mockRemoteVideoTrackOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedAudioTracks() {
        remoteParticipant.getRemoteAudioTracks().add(mockRemoteAudioTrack);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedVideoTracks() {
        remoteParticipant.getRemoteVideoTracks().add(mockRemoteVideoTrackOne);
    }
}
