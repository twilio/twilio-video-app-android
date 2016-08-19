package com.twilio.video;

public class AudioOptions {
    private final boolean echoCancellation;
    private final boolean autoGainControl;
    private final boolean noiseSuppression;
    private final boolean highpassFilter;
    private final boolean stereoSwapping;
    private final int audioJitter;
    private final boolean audioJitterBufferFastAccelerate;
    private final boolean typingDetection;
    private final boolean aecmGenerateComfortNoise;
    private final int adjustAgcDelta;
    private final boolean extendedFilterAec;
    private final boolean delayAgnosticAec;
    private final boolean intelligibilityEnhancer;

    private AudioOptions(Builder builder) {
        echoCancellation = builder.echoCancellation;
        autoGainControl = builder.autoGainControl;
        noiseSuppression = builder.noiseSuppression;
        highpassFilter = builder.highpassFilter;
        stereoSwapping = builder.stereoSwapping;
        audioJitter = builder.audioJitter;
        audioJitterBufferFastAccelerate = builder.audioJitterBufferFastAccelerate;
        typingDetection = builder.typingDetection;
        aecmGenerateComfortNoise = builder.aecmGenerateComfortNoise;
        adjustAgcDelta = builder.adjustAgcDelta;
        extendedFilterAec = builder.extendedFilterAec;
        delayAgnosticAec = builder.delayAgnosticAec;
        intelligibilityEnhancer = builder.intelligibilityEnhancer;
    }

    public boolean isEchoCancellation() {
        return echoCancellation;
    }

    public boolean isAutoGainControl() {
        return autoGainControl;
    }

    public boolean isNoiseSuppression() {
        return noiseSuppression;
    }

    public boolean isHighpassFilter() {
        return highpassFilter;
    }

    public boolean isStereoSwapping() {
        return stereoSwapping;
    }

    public int getAudioJitter() {
        return audioJitter;
    }

    public boolean isAudioJitterBufferFastAccelerate() {
        return audioJitterBufferFastAccelerate;
    }

    public boolean isTypingDetection() {
        return typingDetection;
    }

    public boolean isAecmGenerateComfortNoise() {
        return aecmGenerateComfortNoise;
    }

    public int getAdjustAgcDelta() {
        return adjustAgcDelta;
    }

    public boolean isExtendedFilterAec() {
        return extendedFilterAec;
    }

    public boolean isDelayAgnosticAec() {
        return delayAgnosticAec;
    }

    public boolean isIntelligibilityEnhancer() {
        return intelligibilityEnhancer;
    }

    public static final class Builder {
        private boolean echoCancellation;
        private boolean autoGainControl;
        private boolean noiseSuppression;
        private boolean highpassFilter;
        private boolean stereoSwapping;
        private int audioJitter;
        private boolean audioJitterBufferFastAccelerate;
        private boolean typingDetection;
        private boolean aecmGenerateComfortNoise;
        private int adjustAgcDelta;
        private boolean extendedFilterAec;
        private boolean delayAgnosticAec;
        private boolean intelligibilityEnhancer;

        public Builder() {
        }

        public Builder echoCancellation(boolean echoCancellation) {
            this.echoCancellation = echoCancellation;
            return this;
        }

        public Builder autoGainControl(boolean autoGainControl) {
            this.autoGainControl = autoGainControl;
            return this;
        }

        public Builder noiseSuppression(boolean noiseSuppression) {
            this.noiseSuppression = noiseSuppression;
            return this;
        }

        public Builder highpassFilter(boolean highpassFilter) {
            this.highpassFilter = highpassFilter;
            return this;
        }

        public Builder stereoSwapping(boolean stereoSwapping) {
            this.stereoSwapping = stereoSwapping;
            return this;
        }

        public Builder audioJitter(int audioJitter) {
            this.audioJitter = audioJitter;
            return this;
        }

        public Builder audioJitterBufferFastAccelerate(boolean audioJitterBufferFastAccelerate) {
            this.audioJitterBufferFastAccelerate = audioJitterBufferFastAccelerate;
            return this;
        }

        public Builder typingDetection(boolean typingDetection) {
            this.typingDetection = typingDetection;
            return this;
        }

        public Builder aecmGenerateComfortNoise(boolean aecmGenerateComfortNoise) {
            this.aecmGenerateComfortNoise = aecmGenerateComfortNoise;
            return this;
        }

        public Builder adjustAgcDelta(int adjustAgcDelta) {
            this.adjustAgcDelta = adjustAgcDelta;
            return this;
        }

        public Builder extendedFilterAec(boolean extendedFilterAec) {
            this.extendedFilterAec = extendedFilterAec;
            return this;
        }

        public Builder delayAgnosticAec(boolean delayAgnosticAec) {
            this.delayAgnosticAec = delayAgnosticAec;
            return this;
        }

        public Builder intelligibilityEnhancer(boolean intelligibilityEnhancer) {
            this.intelligibilityEnhancer = intelligibilityEnhancer;
            return this;
        }

        public AudioOptions build() {
            return new AudioOptions(this);
        }
    }
}
