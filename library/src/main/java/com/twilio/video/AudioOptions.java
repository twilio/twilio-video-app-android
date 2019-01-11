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

/** Represents options when adding a {@link LocalAudioTrack}. */
public class AudioOptions {
    public final boolean echoCancellation;
    public final boolean autoGainControl;
    public final boolean noiseSuppression;
    public final boolean highpassFilter;
    public final boolean stereoSwapping;
    public final boolean audioJitterBufferFastAccelerate;
    public final boolean typingDetection;

    private AudioOptions(@NonNull Builder builder) {
        echoCancellation = builder.echoCancellation;
        autoGainControl = builder.autoGainControl;
        noiseSuppression = builder.noiseSuppression;
        highpassFilter = builder.highpassFilter;
        stereoSwapping = builder.stereoSwapping;
        audioJitterBufferFastAccelerate = builder.audioJitterBufferFastAccelerate;
        typingDetection = builder.typingDetection;
    }

    @NonNull
    @Override
    public String toString() {
        return "AudioOptions{"
                + "echoCancellation="
                + echoCancellation
                + ", autoGainControl="
                + autoGainControl
                + ", noiseSuppression="
                + noiseSuppression
                + ", highpassFilter="
                + highpassFilter
                + ", stereoSwapping="
                + stereoSwapping
                + ", audioJitterBufferFastAccelerate="
                + audioJitterBufferFastAccelerate
                + ", typingDetection="
                + typingDetection
                + '}';
    }

    /** Builds new {@link AudioOptions}. */
    public static final class Builder {
        private boolean echoCancellation;
        private boolean autoGainControl;
        private boolean noiseSuppression;
        private boolean highpassFilter;
        private boolean stereoSwapping;
        private boolean audioJitterBufferFastAccelerate;
        private boolean typingDetection;

        public Builder() {}

        /** Attempts to filter away the output signal from later inbound pickup. */
        @NonNull
        public Builder echoCancellation(boolean echoCancellation) {
            this.echoCancellation = echoCancellation;
            return this;
        }

        /** Adjust the sensitivity of the local mic dynamically. */
        @NonNull
        public Builder autoGainControl(boolean autoGainControl) {
            this.autoGainControl = autoGainControl;
            return this;
        }

        /** Filter out background noise. */
        @NonNull
        public Builder noiseSuppression(boolean noiseSuppression) {
            this.noiseSuppression = noiseSuppression;
            return this;
        }

        /** Remove background noise of lower frequences. */
        @NonNull
        public Builder highpassFilter(boolean highpassFilter) {
            this.highpassFilter = highpassFilter;
            return this;
        }

        /** Swap left and right audio channels. */
        @NonNull
        public Builder stereoSwapping(boolean stereoSwapping) {
            this.stereoSwapping = stereoSwapping;
            return this;
        }

        /** Enables fast accelerate mode of jitter buffer. */
        @NonNull
        public Builder audioJitterBufferFastAccelerate(boolean audioJitterBufferFastAccelerate) {
            this.audioJitterBufferFastAccelerate = audioJitterBufferFastAccelerate;
            return this;
        }

        /** Enables typing detection. */
        @NonNull
        public Builder typingDetection(boolean typingDetection) {
            this.typingDetection = typingDetection;
            return this;
        }

        @NonNull
        public AudioOptions build() {
            return new AudioOptions(this);
        }
    }
}
