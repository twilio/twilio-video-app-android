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

import static com.twilio.video.util.VideoAssert.assertIsTrackSid;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.support.test.filters.LargeTest;
import com.kevinmost.junit_retry_rule.Retry;
import com.kevinmost.junit_retry_rule.RetryRule;
import com.twilio.video.base.BaseParticipantTest;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.test.BuildConfig;
import com.twilio.video.util.Topology;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
@LargeTest
public class RemoteAudioTrackTopologyParameterizedTest extends BaseParticipantTest {
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {{Topology.P2P}, {Topology.GROUP}, {Topology.GROUP_SMALL}});
    }

    @Rule public final RetryRule retryRule = new RetryRule();
    private final Topology topology;

    public RemoteAudioTrackTopologyParameterizedTest(Topology topology) {
        this.topology = topology;
    }

    @Before
    public void setup() throws InterruptedException {
        super.baseSetup(topology);
    }

    @After
    public void teardown() throws InterruptedException {
        super.teardown();
        assertTrue(MediaFactory.isReleased());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void shouldHaveTrackSid() throws InterruptedException {
        publishAudioTrack();

        // Validate track was added
        List<RemoteAudioTrackPublication> remoteAudioTrackPublications =
                bobRemoteParticipant.getRemoteAudioTracks();
        assertEquals(1, remoteAudioTrackPublications.size());

        // Validate track sid
        assertTrue(remoteAudioTrackPublications.get(0).isTrackSubscribed());
        assertIsTrackSid(remoteAudioTrackPublications.get(0).getTrackSid());
        assertIsTrackSid(remoteAudioTrackPublications.get(0).getRemoteAudioTrack().getSid());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void shouldHaveTrackName() throws InterruptedException {
        publishAudioTrack();

        // Validate track was added
        List<RemoteAudioTrackPublication> remoteAudioTrackPublications =
                bobRemoteParticipant.getRemoteAudioTracks();
        assertEquals(1, remoteAudioTrackPublications.size());

        // Validate track name
        assertTrue(remoteAudioTrackPublications.get(0).isTrackSubscribed());
        assertEquals(bobAudioTrackName, remoteAudioTrackPublications.get(0).getTrackName());
        assertEquals(
                bobAudioTrackName,
                remoteAudioTrackPublications.get(0).getRemoteAudioTrack().getName());
    }

    @Test
    @Retry(times = BuildConfig.MAX_TEST_RETRIES)
    public void shouldAllowEnablePlayback() throws InterruptedException {
        final CallbackHelper.FakeStatsListener statsListener =
                new CallbackHelper.FakeStatsListener();
        publishAudioTrack();

        // Get bobs remote audio track
        RemoteAudioTrackPublication bobRemoteAudioTrackPublication =
                bobRemoteParticipant.getRemoteAudioTracks().get(0);

        // Validate that playback is enabled by default
        assertTrue(bobRemoteAudioTrackPublication.getRemoteAudioTrack().isPlaybackEnabled());

        // Validate that we can disable playback
        bobRemoteAudioTrackPublication.getRemoteAudioTrack().enablePlayback(false);
        assertFalse(bobRemoteAudioTrackPublication.getRemoteAudioTrack().isPlaybackEnabled());

        // Wait to allow audio to flow and stats to contain valid values
        Thread.sleep(3000);

        // Request stats report
        statsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(statsListener);
        statsListener.onStatsLatch.await(20, TimeUnit.SECONDS);

        // Validate that bobs audio level is zero after playback is disabled
        RemoteAudioTrackStats remoteAudioTrackStats =
                statsListener.getStatsReports().get(0).getRemoteAudioTrackStats().get(0);
        assertEquals(0, remoteAudioTrackStats.audioLevel);

        // Now we enable playback
        bobRemoteAudioTrackPublication.getRemoteAudioTrack().enablePlayback(true);

        // Wait to allow audio to flow and stats to contain valid values
        Thread.sleep(3000);

        // Request stats report
        statsListener.onStatsLatch = new CountDownLatch(1);
        aliceRoom.getStats(statsListener);
        statsListener.onStatsLatch.await(20, TimeUnit.SECONDS);

        // Validate that bobs audio level is greater than 0 after playback enabled
        remoteAudioTrackStats =
                statsListener.getStatsReports().get(0).getRemoteAudioTrackStats().get(0);
        assertTrue(remoteAudioTrackStats.audioLevel > 0);
    }

    private void publishAudioTrack() throws InterruptedException {
        aliceParticipantListener.onAudioTrackPublishedLatch = new CountDownLatch(1);
        aliceParticipantListener.onSubscribedToAudioTrackLatch = new CountDownLatch(1);
        bobRemoteParticipant.setListener(aliceParticipantListener);
        bobLocalAudioTrack = LocalAudioTrack.create(mediaTestActivity, true, bobAudioTrackName);
        assertTrue(bobLocalParticipant.publishTrack(bobLocalAudioTrack));
        assertTrue(aliceParticipantListener.onAudioTrackPublishedLatch.await(20, TimeUnit.SECONDS));
        assertTrue(
                aliceParticipantListener.onSubscribedToAudioTrackLatch.await(20, TimeUnit.SECONDS));
    }
}
