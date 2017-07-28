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

import org.junit.Ignore;
import org.junit.Test;

public class PublishedAudioTrackUnitTest {
    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullSid() {
        new PublishedAudioTrack(null, "Fake Id");
    }

    @Ignore("TODO: Re-enable once published tracks have sids for group rooms GSDK-1270")
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptySid() {
        new PublishedAudioTrack("", "Fake Id");
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullTrackId() {
        new PublishedAudioTrack(Constants.MOCK_TRACK_SID, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailWithEmptyTrackId() {
        new PublishedAudioTrack(Constants.MOCK_TRACK_SID, "");
    }

    @Test
    public void shouldSucceedWithValidTrackSid() {
        new PublishedAudioTrack(Constants.MOCK_TRACK_SID, "Fake Id");
    }
}
