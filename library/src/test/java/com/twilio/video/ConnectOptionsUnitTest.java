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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConnectOptionsUnitTest {
    @Mock LocalAudioTrack localAudioTrack;
    @Mock LocalVideoTrack localVideoTrack;

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenTokenIsNull() {
        new ConnectOptions.Builder(null).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenTokenEmptyString() {
        new ConnectOptions.Builder("").build();
    }

    @Test
    public void insightsShouldBeEnabledByDefault() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
                .build();

        assertTrue(connectOptions.isInsightsEnabled());
    }

    @Test
    public void shouldEnableInsights() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
                .enableInsights(true)
                .build();
        assertTrue(connectOptions.isInsightsEnabled());
    }

    @Test
    public void shouldDisableInsights() {
        ConnectOptions connectOptions = new ConnectOptions.Builder("test")
                .enableInsights(false)
                .build();
        assertFalse(connectOptions.isInsightsEnabled());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowReleasedLocalAudioTrack() throws InterruptedException {
        when(localAudioTrack.isReleased())
                .thenReturn(true);
        new ConnectOptions.Builder("token")
                .roomName("room name")
                .audioTracks(Collections.singletonList(localAudioTrack))
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowReleasedLocalVideoTrack() throws InterruptedException {
        when(localVideoTrack.isReleased())
                .thenReturn(true);
        new ConnectOptions.Builder("token")
                .roomName("room name")
                .videoTracks(Collections.singletonList(localVideoTrack))
                .build();
    }

    @Test
    public void shouldAllowEncodingParameters() {
        EncodingParameters encodingParameters = new EncodingParameters(10, 12);
        ConnectOptions connectOptions = new ConnectOptions.Builder("token")
                .encodingParameters(encodingParameters)
                .build();

        assertEquals(encodingParameters, connectOptions.getEncodingParameters());
    }
}
