/*
 * Copyright (C) 2017 Twilio, inc.
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

import com.twilio.video.util.Constants;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Random;

public class RemoteAudioTrackUnitTest {
    private RemoteAudioTrack remoteAudioTrack;

    @Before
    public void setup() {
        remoteAudioTrack = new UnitTestAudioTrack(Constants.MOCK_TRACK_SID,
                true,
                true);
    }

    @Test(expected = IllegalStateException.class)
    public void setWebRtcTrack_shouldFailIfPreviousTrackNotInvalidated() {
        // Set track
        remoteAudioTrack.setWebRtcTrack(Mockito.mock(org.webrtc.AudioTrack.class));

        // Set track without invalidating
        remoteAudioTrack.setWebRtcTrack(Mockito.mock(org.webrtc.AudioTrack.class));
    }

    private static class UnitTestAudioTrack extends RemoteAudioTrack {
        UnitTestAudioTrack(String sid, boolean isEnabled, boolean subscribed) {
            super(sid, "", isEnabled, subscribed);
        }
    }
}
