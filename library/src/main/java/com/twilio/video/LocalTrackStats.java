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

public abstract class LocalTrackStats extends BaseTrackStats {
    /**
     * Total number of bytes sent for this SSRC
     */
    public final long bytesSent;

    /**
     * Total number of RTP packets sent for this SSRC
     */
    public final int packetsSent;

    /**
     * Estimated round trip time for this SSRC based on the RTCP timestamps.
     * Measured in milliseconds.
     */
    public final long roundTripTime;

    protected LocalTrackStats(String trackId, int packetsLost,
                              String codec, String ssrc, double timestamp,
                              long bytesSent, int packetsSent, long roundTripTime) {
        super(trackId, packetsLost, codec, ssrc, timestamp);
        this.bytesSent = bytesSent;
        this.packetsSent = packetsSent;
        this.roundTripTime = roundTripTime;
    }
}
