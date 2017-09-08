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

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnitParamsRunner.class)
public class EncodingParametersUnitTest {
    private static final int MAX_AUDIO_BITRATE = 10;
    private static final int MAX_VIDEO_BITRATE = 12;
    private static final EncodingParameters encodingParameters =
            new EncodingParameters(MAX_AUDIO_BITRATE, MAX_VIDEO_BITRATE);

    @Test
    public void shouldMatchAudioAndVideoBitrates() {
        assertEquals(MAX_AUDIO_BITRATE, encodingParameters.maxAudioBitrate);
        assertEquals(MAX_VIDEO_BITRATE, encodingParameters.maxVideoBitrate);
    }

    @Test
    @Parameters
    public void equals_returnsTrue(EncodingParameters sameEncodingParameters) {
        assertEquals(sameEncodingParameters, encodingParameters);
    }

    @Test
    @Parameters
    public void equals_returnsFalse(@Nullable Object differentEncodingParameters) {
        EncodingParameters differentEncodingParamters = new EncodingParameters(1,
                MAX_VIDEO_BITRATE);

        assertNotEquals(differentEncodingParamters, encodingParameters);
    }

    private Object[] parametersForEquals_returnsTrue() {
        return new Object[] {
                new Object[]{ encodingParameters },
                new Object[]{ new EncodingParameters(MAX_AUDIO_BITRATE, MAX_VIDEO_BITRATE) }
        };
    }

    private Object[] parametersForEquals_returnsFalse() {
        return new Object[] {
                new Object[]{ null },
                new Object[]{ new Object() },
                new Object[]{ new EncodingParameters(1, 2) },
                new Object[]{ new EncodingParameters(MAX_AUDIO_BITRATE, 2) },
                new Object[]{ new EncodingParameters(1, MAX_VIDEO_BITRATE) }
        };
    }
}
