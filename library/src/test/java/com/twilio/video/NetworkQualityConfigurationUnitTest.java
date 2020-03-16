/*
 * Copyright (C) 2020 Twilio, Inc.
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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NetworkQualityConfigurationUnitTest {

    @Test
    public void defaultConstructor() {
        NetworkQualityConfiguration config = new NetworkQualityConfiguration();
        assertEquals(config.local, NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);
        assertEquals(config.remote, NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE);
    }

    @Test(expected = NullPointerException.class)
    public void nullLocalVerbosity() {
        NetworkQualityConfiguration config = new NetworkQualityConfiguration(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLocalLevel() {
        NetworkQualityConfiguration config =
                new NetworkQualityConfiguration(
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE,
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);
    }

    @Test(expected = NullPointerException.class)
    public void nullRemoteVerbosity() {
        NetworkQualityConfiguration config =
                new NetworkQualityConfiguration(
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL, null);
    }

    @Test
    public void localMinimalRemoteNone() {
        NetworkQualityConfiguration config =
                new NetworkQualityConfiguration(
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE);
        assertEquals(config.local, NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);
        assertEquals(config.remote, NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_NONE);
    }

    @Test
    public void localMinimalRemoteMinimal() {
        NetworkQualityConfiguration config =
                new NetworkQualityConfiguration(
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL,
                        NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);
        assertEquals(config.local, NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);
        assertEquals(config.remote, NetworkQualityVerbosity.NETWORK_QUALITY_VERBOSITY_MINIMAL);
    }
}
