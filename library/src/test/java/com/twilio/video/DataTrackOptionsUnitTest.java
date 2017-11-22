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

import java.util.UUID;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class DataTrackOptionsUnitTest {
    @Test(expected = IllegalArgumentException.class)
    @Parameters({"-10", "65536"})
    public void invalidMaxPacketLifeShouldFail(int maxPacketLifeTime) {
        new DataTrackOptions.Builder()
                .maxPacketLifeTime(maxPacketLifeTime)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters({"-10", "65536"})
    public void invalidMaxRetransmitsShouldFail(int maxRetransmits) {
        new DataTrackOptions.Builder()
                .maxRetransmits(maxRetransmits)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void settingMaxRetransmitsAndMaxPacketLifeShouldFail() {
        new DataTrackOptions.Builder()
                .maxRetransmits(5)
                .maxPacketLifeTime(10)
                .build();
    }

    @Test
    public void shouldAllowValidOptions() {
        boolean expectedOrdered = false;
        int expectedMaxPacketLifeTime = 10;
        String expectedName = UUID.randomUUID().toString();
        DataTrackOptions dataTrackOptions = new DataTrackOptions.Builder()
                .ordered(expectedOrdered)
                .maxPacketLifeTime(expectedMaxPacketLifeTime)
                .name(expectedName)
                .build();

        assertEquals(expectedOrdered, dataTrackOptions.ordered);
        assertEquals(expectedMaxPacketLifeTime, dataTrackOptions.maxPacketLifeTime);
        assertEquals(DataTrackOptions.DEFAULT_MAX_RETRANSMITS, dataTrackOptions.maxRetransmits);
        assertEquals(expectedName, dataTrackOptions.name);
    }
}
