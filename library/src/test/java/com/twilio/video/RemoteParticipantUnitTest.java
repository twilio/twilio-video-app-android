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
import java.util.Arrays;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteParticipantUnitTest {
    private static final int INT_MAX = 25;

    private final Random random = new Random();
    private RemoteParticipant remoteParticipant;
    @Mock RemoteAudioTrackPublication mockRemoteAudioTrackPublication;
    @Mock RemoteVideoTrackPublication mockRemoteVideoTrackPublicationOne;
    @Mock RemoteVideoTrackPublication mockRemoteVideoTrackPublicationTwo;
    @Mock RemoteDataTrackPublication mockRemoteDataTrackPublication;
    @Mock Handler handler;

    @Before
    public void setup() {
        remoteParticipant =
                new RemoteParticipant(
                        String.valueOf(random.nextInt(INT_MAX)),
                        String.valueOf(random.nextInt(INT_MAX)),
                        Arrays.asList(mockRemoteAudioTrackPublication),
                        Arrays.asList(
                                mockRemoteVideoTrackPublicationOne,
                                mockRemoteVideoTrackPublicationTwo),
                        Arrays.asList(mockRemoteDataTrackPublication),
                        handler,
                        random.nextLong());
    }

    @Test(expected = NullPointerException.class)
    public void setListener_shouldNotAllowNull() throws Exception {
        remoteParticipant.setListener(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingAudioTracks() {
        remoteParticipant.getAudioTracks().add(mockRemoteAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingVideoTracks() {
        remoteParticipant.getVideoTracks().add(mockRemoteVideoTrackPublicationOne);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedAudioTracks() {
        remoteParticipant.getRemoteAudioTracks().add(mockRemoteAudioTrackPublication);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowModifyingSubscribedVideoTracks() {
        remoteParticipant.getRemoteVideoTracks().add(mockRemoteVideoTrackPublicationOne);
    }
}
