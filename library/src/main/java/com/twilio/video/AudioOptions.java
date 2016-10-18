package com.twilio.video;

/**
 * Represents options when adding a {@link LocalAudioTrack}.
 */
public class AudioOptions {
    public final boolean echoCancellation;
    public final boolean autoGainControl;
    public final boolean noiseSuppression;
    public final boolean highpassFilter;
    public final boolean stereoSwapping;
    public final boolean audioJitterBufferFastAccelerate;
    public final boolean typingDetection;

    public AudioOptions(Builder builder) {
        echoCancellation = builder.echoCancellation;
        autoGainControl = builder.autoGainControl;
        noiseSuppression = builder.noiseSuppression;
        highpassFilter = builder.highpassFilter;
        stereoSwapping = builder.stereoSwapping;
        audioJitterBufferFastAccelerate = builder.audioJitterBufferFastAccelerate;
        typingDetection = builder.typingDetection;
    }

    /**
     * Builds new {@link AudioOptions}.
     */
    public static final class Builder {
        private boolean echoCancellation;
        private boolean autoGainControl;
        private boolean noiseSuppression;
        private boolean highpassFilter;
        private boolean stereoSwapping;
        private boolean audioJitterBufferFastAccelerate;
        private boolean typingDetection;

        public Builder() {}

        /**
         * Attempts to filter away the output signal from later inbound pickup.
         */
        public Builder echoCancellation(boolean echoCancellation) {
            this.echoCancellation = echoCancellation;
            return this;
        }

        /**
         * Adjust the sensitivity of the local mic dynamically.
         */
        public Builder autoGainControl(boolean autoGainControl) {
            this.autoGainControl = autoGainControl;
            return this;
        }

        /**
         * Filter out background noise.
         */
        public Builder noiseSuppression(boolean noiseSuppression) {
            this.noiseSuppression = noiseSuppression;
            return this;
        }

        /**
         * Remove background noise of lower frequences.
         */
        public Builder highpassFilter(boolean highpassFilter) {
            this.highpassFilter = highpassFilter;
            return this;
        }

        /**
         * Swap left and right audio channels.
         */
        public Builder stereoSwapping(boolean stereoSwapping) {
            this.stereoSwapping = stereoSwapping;
            return this;
        }

        /**
         * Enables fast accelerate mode of jitter buffer.
         */
        public Builder audioJitterBufferFastAccelerate(boolean audioJitterBufferFastAccelerate) {
            this.audioJitterBufferFastAccelerate = audioJitterBufferFastAccelerate;
            return this;
        }

        /**
         * Enables typing detection.
         */
        public Builder typingDetection(boolean typingDetection) {
            this.typingDetection = typingDetection;
            return this;
        }

        public AudioOptions build() {
            return new AudioOptions(this);
        }
    }
}
