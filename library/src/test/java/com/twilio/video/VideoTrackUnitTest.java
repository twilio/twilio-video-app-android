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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VideoTrackUnitTest {
    @Mock org.webrtc.VideoTrack mockWebRtcVideoTrack;
    @Mock VideoRenderer videoRenderer;
    private VideoTrack videoTrack;

    @Before
    public void setup() {
        videoTrack = new UnitTestVideoTrack(mockWebRtcVideoTrack, true);
    }

    @Test(expected = NullPointerException.class)
    public void addRenderer_shouldNotAllowNull() {
        videoTrack.addRenderer(null);
    }

    @Test(expected = NullPointerException.class)
    public void removeRenderer_shouldNotAllowNull() {
        videoTrack.removeRenderer(null);
    }

    @Test(expected = IllegalStateException.class)
    public void setWebRtcTrack_shouldFailIfPreviousTrackNotInvalidated() {
        videoTrack.setWebRtcTrack(Mockito.mock(org.webrtc.VideoTrack.class));
    }

    /*
     * Simple concrete video track to test functionality in abstract class.
     */
    private static class UnitTestVideoTrack extends VideoTrack {
        UnitTestVideoTrack(org.webrtc.VideoTrack webRtcVideoTrack, boolean enabled) {
            super(webRtcVideoTrack, enabled);
        }
    }
}
