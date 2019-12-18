/*
 * Copyright (C) 2019 Twilio, Inc.
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

package com.twilio.video.app.model;

import com.twilio.video.BaseTrackStats;

public class StatsListItem {
    public final String trackSid;
    public final String trackName;
    public final String codec;
    public final int packetsLost;
    public final long bytes;
    public final long rtt;
    public final String dimensions;
    public final int framerate;
    public final int jitter;
    public final int audioLevel;
    public final boolean isLocalTrack;
    public final boolean isAudioTrack;

    private StatsListItem(Builder builder) {
        this.trackSid = builder.trackSid;
        this.trackName = builder.trackName;
        this.codec = builder.codec;
        this.packetsLost = builder.packetsLost;
        this.bytes = builder.bytes;
        this.rtt = builder.rtt;
        this.dimensions = builder.dimensions;
        this.framerate = builder.framerate;
        this.jitter = builder.jitter;
        this.audioLevel = builder.audioLevel;
        this.isLocalTrack = builder.isLocalTrack;
        this.isAudioTrack = builder.isAudioTrack;
    }

    public static class Builder {
        private String trackSid;
        private String trackName;
        private String codec;
        private int packetsLost;
        private long bytes;
        private long rtt;
        private String dimensions;
        private int framerate;
        private int jitter;
        private int audioLevel;
        private boolean isLocalTrack;
        private boolean isAudioTrack;

        public Builder() {}

        public Builder trackName(String trackName) {
            this.trackName = trackName;
            return this;
        }

        public Builder bytes(long bytes) {
            this.bytes = bytes;
            return this;
        }

        public Builder rtt(long rtt) {
            this.rtt = rtt;
            return this;
        }

        public Builder dimensions(String dimensions) {
            this.dimensions = dimensions;
            return this;
        }

        public Builder framerate(int framerate) {
            this.framerate = framerate;
            return this;
        }

        public Builder jitter(int jitter) {
            this.jitter = jitter;
            return this;
        }

        public Builder audioLevel(int audioLevel) {
            this.audioLevel = audioLevel;
            return this;
        }

        public Builder isLocalTrack(boolean isLocalTrack) {
            this.isLocalTrack = isLocalTrack;
            return this;
        }

        public Builder isAudioTrack(boolean isAudioTrack) {
            this.isAudioTrack = isAudioTrack;
            return this;
        }

        public Builder baseTrackInfo(BaseTrackStats trackStats) {
            this.codec = trackStats.codec;
            this.packetsLost = trackStats.packetsLost;
            this.trackSid = trackStats.trackSid;
            return this;
        }

        public StatsListItem build() {
            return new StatsListItem(this);
        }
    }
}
