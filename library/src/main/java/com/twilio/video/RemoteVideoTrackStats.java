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

import android.support.annotation.NonNull;

public class RemoteVideoTrackStats extends RemoteTrackStats {
    /** Received frame dimensions */
    @NonNull public final VideoDimensions dimensions;

    /** Received frame rate */
    public final int frameRate;

    RemoteVideoTrackStats(
            @NonNull String trackSid,
            int packetsLost,
            @NonNull String codec,
            @NonNull String ssrc,
            double timestamp,
            long bytesReceived,
            int packetsReceived,
            @NonNull VideoDimensions dimensions,
            int frameRate) {
        super(trackSid, packetsLost, codec, ssrc, timestamp, bytesReceived, packetsReceived);
        this.dimensions = dimensions;
        this.frameRate = frameRate;
    }
}
