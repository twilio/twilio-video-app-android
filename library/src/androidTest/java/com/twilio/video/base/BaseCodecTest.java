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

package com.twilio.video.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.support.annotation.Nullable;
import com.twilio.video.AudioCodec;
import com.twilio.video.LocalAudioTrackStats;
import com.twilio.video.LocalVideoTrackStats;
import com.twilio.video.RemoteAudioTrackStats;
import com.twilio.video.RemoteVideoTrackStats;
import com.twilio.video.StatsReport;
import com.twilio.video.VideoCodec;
import com.twilio.video.helper.CallbackHelper;
import com.twilio.video.util.StringUtils;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class BaseCodecTest extends BaseStatsTest {
    private static int CODEC_TEST_MAX_RETRIES = 10;

    protected void assertAudioCodecPublished(@Nullable AudioCodec expectedAudioCodec)
            throws InterruptedException {
        assertCodecsPublished(expectedAudioCodec, null);
    }

    protected void assertVideoCodecPublished(@Nullable VideoCodec expectedAudioCodec)
            throws InterruptedException {
        assertCodecsPublished(null, expectedAudioCodec);
    }

    protected void assertCodecsPublished(
            @Nullable AudioCodec expectedAudioCodec, @Nullable VideoCodec expectedVideoCodec)
            throws InterruptedException {
        /*
         * The stats API is not very predictable so retry getting stats and until a non empty
         * value for codecs are available.
         */
        String localAudioTrackCodec = null;
        String localVideoTrackCodec = null;
        String remoteAudioTrackCodec = null;
        String remoteVideoTrackCodec = null;
        boolean failedToGetNeededCodecs;
        int retries = 0;
        do {
            // Give peer connection some time to get media flowing
            Thread.sleep(1000);

            // Get stats for alice and bob
            CallbackHelper.FakeStatsListener aliceStatsListener =
                    new CallbackHelper.FakeStatsListener();
            CallbackHelper.FakeStatsListener bobStatsListener =
                    new CallbackHelper.FakeStatsListener();
            aliceStatsListener.onStatsLatch = new CountDownLatch(1);
            bobStatsListener.onStatsLatch = new CountDownLatch(1);
            aliceRoom.getStats(aliceStatsListener);
            bobRoom.getStats(bobStatsListener);
            assertTrue(aliceStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));
            assertTrue(bobStatsListener.onStatsLatch.await(20, TimeUnit.SECONDS));

            // Extract local and remote audio and video codecs
            if (expectedAudioCodec != null) {
                StatsReport aliceStatsReport = aliceStatsListener.getStatsReports().get(0);
                StatsReport bobStatsReport = bobStatsListener.getStatsReports().get(0);
                List<LocalAudioTrackStats> aliceLocalAudioTrackStats =
                        aliceStatsReport.getLocalAudioTrackStats();
                List<RemoteAudioTrackStats> bobRemoteAudioTrackStats =
                        bobStatsReport.getRemoteAudioTrackStats();

                if (!aliceLocalAudioTrackStats.isEmpty()) {
                    localAudioTrackCodec = aliceLocalAudioTrackStats.get(0).codec.toLowerCase();
                }

                if (!bobRemoteAudioTrackStats.isEmpty()) {
                    remoteAudioTrackCodec = bobRemoteAudioTrackStats.get(0).codec.toLowerCase();
                }
            }

            if (expectedVideoCodec != null) {
                StatsReport aliceStatsReport = aliceStatsListener.getStatsReports().get(0);
                StatsReport bobStatsReport = bobStatsListener.getStatsReports().get(0);
                List<LocalVideoTrackStats> aliceLocalVideoTrackStats =
                        aliceStatsReport.getLocalVideoTrackStats();
                List<RemoteVideoTrackStats> bobRemoteVideoTrackStats =
                        bobStatsReport.getRemoteVideoTrackStats();

                if (!aliceLocalVideoTrackStats.isEmpty()) {
                    localVideoTrackCodec = aliceLocalVideoTrackStats.get(0).codec.toLowerCase();
                }

                if (!bobRemoteVideoTrackStats.isEmpty()) {
                    remoteVideoTrackCodec = bobRemoteVideoTrackStats.get(0).codec.toLowerCase();
                }
            }

            // Check if all codecs are set
            failedToGetNeededCodecs =
                    (expectedAudioCodec != null && StringUtils.isNullOrEmpty(localAudioTrackCodec))
                            || (expectedAudioCodec != null
                                    && StringUtils.isNullOrEmpty(remoteAudioTrackCodec))
                            || (expectedVideoCodec != null
                                    && StringUtils.isNullOrEmpty(localVideoTrackCodec))
                            || (expectedVideoCodec != null
                                    && StringUtils.isNullOrEmpty(remoteVideoTrackCodec));
        } while (failedToGetNeededCodecs && retries++ < CODEC_TEST_MAX_RETRIES);

        // Validate codecs
        if (expectedAudioCodec != null) {
            assertEquals(expectedAudioCodec.getName().toLowerCase(), localAudioTrackCodec);
            assertEquals(expectedAudioCodec.getName().toLowerCase(), remoteAudioTrackCodec);
        }

        if (expectedVideoCodec != null) {
            assertEquals(expectedVideoCodec.getName().toLowerCase(), localVideoTrackCodec);
            assertEquals(expectedVideoCodec.getName().toLowerCase(), remoteVideoTrackCodec);
        }
    }
}
