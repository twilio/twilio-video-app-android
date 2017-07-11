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

public abstract class BaseTrackStats {
    /**
     * Track identifier
     */
    public final String trackId;

    /**
     * Total number of RTP packets lost for this SSRC since
     * the beginning of the reception.
     */
    public final int packetsLost;

    /**
     * Name of codec used for this track
     */
    public final String codec;

    /**
     * The SSRC identifier of the source
     */
    public final String ssrc;

    /**
     * Unix timestamp in milliseconds
     */
    public final double timestamp;

    protected BaseTrackStats(String trackId, int packetsLost,
                             String codec, String ssrc, double timestamp) {
        this.trackId = trackId;
        this.packetsLost = packetsLost;
        this.codec = codec;
        this.ssrc = ssrc;
        this.timestamp = timestamp;
    }
}
