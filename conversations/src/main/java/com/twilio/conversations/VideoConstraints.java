package com.twilio.conversations;

public class VideoConstraints {
    public static final int HD_VIDEO_WIDTH = 1280;
    public static final int HD_VIDEO_HEIGHT = 720;
    public static final VideoDimensions HD_VIDEO_DIMENSIONS = new VideoDimensions(HD_VIDEO_WIDTH, HD_VIDEO_HEIGHT);

    public static final int MAX_VIDEO_WIDTH = 1280;
    public static final int MAX_VIDEO_HEIGHT = 1280;
    public static final VideoDimensions MAX_VIDEO_DIMENSIONS = new VideoDimensions(MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT);

    public static final int MIN_VIDEO_FPS = 1;
    public static final int MAX_VIDEO_FPS = 30;

    private final VideoDimensions minVideoDimensions;
    private final VideoDimensions maxVideoDimensions;
    private final int minFPS;
    private final int maxFPS;

    private VideoConstraints(VideoDimensions minVideoDimensions, VideoDimensions maxVideoDimensions, int minFPS, int maxFPS) {
        this.minVideoDimensions = minVideoDimensions;
        this.maxVideoDimensions = maxVideoDimensions;
        this.minFPS = minFPS;
        this.maxFPS = maxFPS;
    }

    public VideoDimensions getMinVideoDimensions() {
        return minVideoDimensions;
    }

    public VideoDimensions getMaxVideoDimensions() {
        return maxVideoDimensions;
    }

    public int getMinFPS() {
        return minFPS;
    }

    public int getMaxFPS() {
        return maxFPS;
    }

    public static class Builder {
        private VideoDimensions minVideoDimensions;
        private VideoDimensions maxVideoDimensions = new VideoDimensions(VideoConstraints.MAX_VIDEO_WIDTH, VideoConstraints.MAX_VIDEO_HEIGHT);
        private int minFPS = 1;
        private int maxFPS = 30;

        public Builder() { }

        public Builder setMinVideoDimensions(VideoDimensions minVideoDimensions) {
            this.minVideoDimensions = minVideoDimensions;
            return this;
        }

        public Builder setMaxVideoDimensions(VideoDimensions maxVideoDimensions) {
            this.maxVideoDimensions = maxVideoDimensions;
            return this;
        }

        public Builder setMinFPS(int minFPS) {
            this.minFPS = minFPS;
            return this;
        }

        public Builder setMaxFPS(int maxFPS) {
            this.maxFPS = maxFPS;
            return this;
        }

        public VideoConstraints build() {
            return new VideoConstraints(minVideoDimensions, maxVideoDimensions, minFPS, maxFPS);
        }
    }

}
