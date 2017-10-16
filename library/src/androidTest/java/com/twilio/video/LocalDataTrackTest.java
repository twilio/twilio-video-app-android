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


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalDataTrackTest {
    private Context context;
    private LocalDataTrack localDataTrack;

    @Before
    public void setup() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void teardown() {
        if (localDataTrack != null) {
            localDataTrack.release();
        }
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    public void canCreateDataTrack() {
        localDataTrack = LocalDataTrack.create(context);

        assertNotNull(localDataTrack);
        assertFalse(localDataTrack.getTrackId().isEmpty());
        assertEquals(true, localDataTrack.isEnabled());
        assertEquals(true, localDataTrack.isOrdered());
        assertEquals(0, localDataTrack.getMaxPacketLifeTime());
        assertEquals(0, localDataTrack.getMaxRetransmits());
    }

    @Test
    public void canCreateDataTrackWithName() {
        String expectedName = random(10);
        localDataTrack = LocalDataTrack.create(context, expectedName);

        assertNotNull(localDataTrack);
        assertEquals(expectedName, localDataTrack.getName());
    }

    @Test
    public void canCreateDataTrackWithNullName() {
        String nullName = null;
        String expectedName = "";
        localDataTrack = LocalDataTrack.create(context, nullName);

        assertNotNull(localDataTrack);
        assertEquals(expectedName, localDataTrack.getName());
    }

    @Test
    public void canCreateDataTrackWithEmptyName() {
        String emptyName = "";
        String expectedName = "";
        localDataTrack = LocalDataTrack.create(context, emptyName);

        assertNotNull(localDataTrack);
        assertEquals(expectedName, localDataTrack.getName());
    }

    @Test
    public void canCreateUnreliableDataTrack() {
        int expectedMaxPacketLifeTime = 1000;
        DataTrackOptions dataTrackOptions = new DataTrackOptions.Builder()
                .ordered(false)
                .maxPacketLifeTime(expectedMaxPacketLifeTime)
                .build();
        localDataTrack = LocalDataTrack.create(context, dataTrackOptions);

        assertFalse(localDataTrack.isOrdered());
        assertFalse(localDataTrack.isReliable());
        assertEquals(expectedMaxPacketLifeTime, localDataTrack.getMaxPacketLifeTime());
    }

    @Test
    public void canCreateReliableDataTrack() {
        DataTrackOptions dataTrackOptions = new DataTrackOptions.Builder()
                .ordered(true)
                .maxPacketLifeTime(DataTrackOptions.DEFAULT_MAX_PACKET_LIFE_TIME)
                .maxRetransmits(DataTrackOptions.DEFAULT_MAX_RETRANSMITS)
                .build();
        localDataTrack = LocalDataTrack.create(context, dataTrackOptions);

        assertTrue(localDataTrack.isOrdered());
        assertTrue(localDataTrack.isReliable());
    }

    @Test
    public void isEnabled_shouldReturnFalseAfterReleased() {
        localDataTrack = LocalDataTrack.create(context);

        assertNotNull(localDataTrack);
        assertTrue(localDataTrack.isEnabled());
        localDataTrack.release();
        assertFalse(localDataTrack.isEnabled());
    }

    @Test
    public void canReleaseDataTrack() {
        localDataTrack = LocalDataTrack.create(context);

        assertNotNull(localDataTrack);
        localDataTrack.release();
    }

    @Test
    public void release_shouldBeIdempotent() {
        localDataTrack = LocalDataTrack.create(context);

        assertNotNull(localDataTrack);
        localDataTrack.release();
        localDataTrack.release();
    }

    @Test
    public void canCreateMultipleDataTracks() {
        int numDataTracks = 5;

        for (int i = 0 ; i < numDataTracks ; i++) {
            LocalDataTrack localDataTrack = LocalDataTrack.create(context);

            assertNotNull(localDataTrack);
            localDataTrack.release();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void sendBuffer_shouldFailAfterReleased() {
        localDataTrack = LocalDataTrack.create(context);
        localDataTrack.release();
        ByteBuffer messageBuffer = ByteBuffer.wrap(new byte[]{ 0x00, 0x11, 0x22, 0x33 });

        localDataTrack.send(messageBuffer);
    }

    @Test(expected = IllegalStateException.class)
    public void sendString_shouldFailAfterReleased() {
        localDataTrack = LocalDataTrack.create(context);
        localDataTrack.release();
        String message = "Hello World!";

        localDataTrack.send(message);
    }

    @Test(expected = NullPointerException.class)
    public void send_shouldFailWithNullBuffer() {
        localDataTrack = LocalDataTrack.create(context);
        ByteBuffer nullByteBuffer = null;

        localDataTrack.send(nullByteBuffer);
    }

    @Test(expected = NullPointerException.class)
    public void send_shouldFailWithNullString() {
        localDataTrack = LocalDataTrack.create(context);
        String nullString = null;

        localDataTrack.send(nullString);
    }

    @Test
    public void canSendBufferIfNotPublished() {
        localDataTrack = LocalDataTrack.create(context);
        ByteBuffer messageBuffer = ByteBuffer.wrap(new byte[]{ 0x00, 0x11, 0x22, 0x33 });

        localDataTrack.send(messageBuffer);
    }

    @Test
    public void canSendMessageIfNotPublished() {
        localDataTrack = LocalDataTrack.create(context);
        String message = "Hello World!";

        localDataTrack.send(message);
    }
}
