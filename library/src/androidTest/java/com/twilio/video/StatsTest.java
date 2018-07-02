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

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.twilio.video.base.BaseStatsTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.Topology;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StatsTest extends BaseStatsTest {
    @Before
    public void setup() throws InterruptedException {
        // Topology does not matter in these tests
        super.baseSetup(Topology.P2P);
    }

    @After
    @Override
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailWithNullListener() throws InterruptedException {
        aliceRoom = createRoom(aliceToken, aliceListener, roomName);
        aliceRoom.getStats(null);
    }

    @Test
    public void shouldReceiveStatsInEmptyRoom() throws InterruptedException {
        aliceRoom = createRoom(aliceToken, aliceListener, roomName);

        CallbackHelper.FakeStatsListener aliceStatsListener =
                new CallbackHelper.FakeStatsListener();
        aliceStatsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(aliceStatsListener);
        assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
        assertTrue(1 >= aliceStatsListener.getStatsReports().size());
    }

    @Test
    public void shouldInvokeListenerOnCallingThread() throws InterruptedException {
        // Connect Alice to room with local audio track only
        aliceLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true);
        aliceRoom = createRoom(aliceToken,
                aliceListener,
                roomName,
                Collections.singletonList(aliceLocalAudioTrack));
        aliceListener.onParticipantConnectedLatch = new CountDownLatch(1);
        final CountDownLatch statsCallback = new CountDownLatch(1);

        /*
         * Run on UI thread to avoid thread hopping between the test runner thread and the UI
         * thread.
         */
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                final long callingThreadId = Thread.currentThread().getId();
                StatsListener statsListener = new StatsListener() {
                    @Override
                    public void onStats(List<StatsReport> statsReports) {
                        assertEquals(callingThreadId, Thread.currentThread().getId());
                        statsCallback.countDown();
                    }
                };
                aliceRoom.getStats(statsListener);
            }
        });

        assertTrue(statsCallback.await(20, TimeUnit.SECONDS));
    }
}
