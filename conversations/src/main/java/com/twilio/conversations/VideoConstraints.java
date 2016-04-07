package com.twilio.conversations;

public class VideoConstraints {

    // Battery saving 10 fps video
    public static final int FRAME_RATE_10 = 10;
    // Battery saving 15 fps video
    public static final int FRAME_RATE_15 = 15;
    // Battery efficient 20 fps video
    public static final int FRAME_RATE_20 = 20;
    // Cinematic 24 fps video
    public static final int FRAME_RATE_24 = 24;
    // Smooth 30 fps video
    public static final int FRAME_RATE_30 = 30;

    private final VideoDimensions minVideoDimensions;
    private final VideoDimensions maxVideoDimensions;
    private final int minFps;
    private final int maxFps;

    private VideoConstraints(Builder builder) {
        this.minVideoDimensions = builder.minVideoDimensions;
        this.maxVideoDimensions = builder.maxVideoDimensions;
        this.minFps = builder.minFps;
        this.maxFps = builder.maxFps;
    }

    public VideoDimensions getMinVideoDimensions() {
        return minVideoDimensions;
    }

    public VideoDimensions getMaxVideoDimensions() {
        return maxVideoDimensions;
    }

    public int getMinFps() {
        return minFps;
    }

    public int getMaxFps() {
        return maxFps;
    }

    public static class Builder {
        private VideoDimensions minVideoDimensions = new VideoDimensions(0,0);
        private VideoDimensions maxVideoDimensions = new VideoDimensions(0,0);
        private int minFps = 0;
        private int maxFps = 0;

        public Builder() { }

        public Builder minVideoDimensions(VideoDimensions minVideoDimensions) {
            this.minVideoDimensions = minVideoDimensions;
            return this;
        }

        public Builder maxVideoDimensions(VideoDimensions maxVideoDimensions) {
            this.maxVideoDimensions = maxVideoDimensions;
            return this;
        }

        public Builder minFps(int minFps) {
            this.minFps = minFps;
            return this;
        }

        public Builder maxFps(int maxFps) {
            this.maxFps = maxFps;
            return this;
        }

        public VideoConstraints build() {
            if(minVideoDimensions == null) {
               throw new NullPointerException("MinVideoDimensions must not be null");
            }
            if(maxVideoDimensions == null) {
                throw new NullPointerException("MaxVideoDimensions must not be null");
            }
            if(minFps > maxFps) {
                throw new IllegalStateException("MinFps " + minFps + " is greater than maxFps " + maxFps);
            }
            if(minFps < 0) {
                throw new IllegalStateException("MinFps is less than 0");
            }
            if(maxFps < 0) {
                throw new IllegalStateException("MaxFps is less than 0");
            }
            if(minFps > maxFps) {
                throw new IllegalStateException("MinFps is greater than maxFps");
            }
            if(minVideoDimensions.width > maxVideoDimensions.width) {
                throw new IllegalStateException("Min video dimensions width " + minVideoDimensions.width + " is greater than max video dimensions width " + maxVideoDimensions.width);
            }
            if(minVideoDimensions.height > maxVideoDimensions.height) {
                throw new IllegalStateException("Min video dimensions height " + minVideoDimensions.height+ " is greater than max video dimensions height " + maxVideoDimensions.height);
            }
            return new VideoConstraints(this);
        }
    }

}
