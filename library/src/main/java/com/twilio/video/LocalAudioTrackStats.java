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

public class LocalAudioTrackStats extends LocalTrackStats {
    /** Audio input level */
    public final int audioLevel;

    /** Packet jitter measured in milliseconds */
    public final int jitter;

    LocalAudioTrackStats(
            String trackSid,
            int packetsLost,
            String codec,
            String ssrc,
            double timestamp,
            long bytesSent,
            int packetsSent,
            long roundTripTime,
            int audioLevel,
            int jitter) {
        super(trackSid, packetsLost, codec, ssrc, timestamp, bytesSent, packetsSent, roundTripTime);
        this.audioLevel = audioLevel;
        this.jitter = jitter;
    }
}
