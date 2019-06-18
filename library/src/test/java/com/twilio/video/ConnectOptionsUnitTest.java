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

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        ConnectOptions connectOptions = new ConnectOptions.Builder("test").build();

        assertTrue(connectOptions.isInsightsEnabled());
    }

    @Test
    public void shouldEnableInsights() {
        ConnectOptions connectOptions =
                new ConnectOptions.Builder("test").enableInsights(true).build();
        assertTrue(connectOptions.isInsightsEnabled());
    }

    @Test
    public void shouldDisableInsights() {
        ConnectOptions connectOptions =
                new ConnectOptions.Builder("test").enableInsights(false).build();
        assertFalse(connectOptions.isInsightsEnabled());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowReleasedLocalAudioTrack() {
        when(localAudioTrack.isReleased()).thenReturn(true);
        new ConnectOptions.Builder("token")
                .roomName("room name")
                .audioTracks(Collections.singletonList(localAudioTrack))
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotAllowReleasedLocalVideoTrack() {
        when(localVideoTrack.isReleased()).thenReturn(true);
        new ConnectOptions.Builder("token")
                .roomName("room name")
                .videoTracks(Collections.singletonList(localVideoTrack))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowCustomAudioCodec() {
        AudioCodec customAudioCodec = new AudioCodec("custom audio codec") {};

        new ConnectOptions.Builder("token")
                .preferAudioCodecs(Collections.singletonList(customAudioCodec))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowCustomVideoCodec() {
        VideoCodec customVideoCodec = new VideoCodec("custom video codec") {};

        new ConnectOptions.Builder("token")
                .preferVideoCodecs(Collections.singletonList(customVideoCodec))
                .build();
    }

    @Test
    public void shouldAllowEncodingParameters() {
        EncodingParameters encodingParameters = new EncodingParameters(10, 12);
        ConnectOptions connectOptions =
                new ConnectOptions.Builder("token").encodingParameters(encodingParameters).build();

        assertEquals(encodingParameters, connectOptions.getEncodingParameters());
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullVideoCodecs() {
        List<VideoCodec> codecs = new ArrayList<>();
        codecs.add(new Vp8Codec());
        codecs.add(null);
        new ConnectOptions.Builder("token").preferVideoCodecs(codecs).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullAudioCodecs() {
        List<AudioCodec> codecs = new ArrayList<>();
        codecs.add(new OpusCodec());
        codecs.add(null);
        new ConnectOptions.Builder("token").preferAudioCodecs(codecs).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullAudioTracks() {
        new ConnectOptions.Builder("token").audioTracks(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullVideoTracks() {
        new ConnectOptions.Builder("token").videoTracks(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullDataTracks() {
        new ConnectOptions.Builder("token").dataTracks(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullRoomName() {
        new ConnectOptions.Builder("token").roomName(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullIceOptions() {
        new ConnectOptions.Builder("token").iceOptions(null).build();
    }

    @Test(expected = NullPointerException.class)
    public void shouldNotAllowNullEncodingParameters() {
        new ConnectOptions.Builder("token").encodingParameters(null).build();
    }
}
