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

public class RemoteAudioTrackStats extends RemoteTrackStats {
    /**
     * Audio output level
     */
    public final int audioLevel;

    /**
     * Packet jitter measured in milliseconds
     */
    public final int jitter;

    RemoteAudioTrackStats(String trackSid,
                          int packetsLost,
                          String codec,
                          String ssrc,
                          double timestamp,
                          long bytesReceived,
                          int packetsReceived,
                          int audioLevel,
                          int jitter) {
        super(trackSid, packetsLost, codec, ssrc,
                timestamp, bytesReceived, packetsReceived);
        this.audioLevel = audioLevel;
        this.jitter = jitter;
    }
}
