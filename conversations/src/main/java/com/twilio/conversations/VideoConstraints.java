package com.twilio.conversations;

import android.graphics.Point;

public class VideoConstraints {
    public static final int HD_VIDEO_WIDTH = 1280;
    public static final int HD_VIDEO_HEIGHT = 720;
    public static final Point HD_VIDEO_DIMENSIONS = new Point(HD_VIDEO_WIDTH, HD_VIDEO_HEIGHT);

    public static final int MAX_VIDEO_WIDTH = 1280;
    public static final int MAX_VIDEO_HEIGHT = 1280;
    public static final Point MAX_VIDEO_DIMENSIONS = new Point(MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT);
    public static final int MIN_VIDEO_FPS = 1;
    public static final int MAX_VIDEO_FPS = 30;

    private final int minVideoWidth;
    private final int minVideoHeight;
    private final int maxVideoWidth;
    private final int maxVideoHeight;
    private final int minFPS;
    private final int maxFPS;

    private VideoConstraints(int minVideoWidth, int minVideoHeight, int maxVideoWidth, int maxVideoHeight, int minFPS, int maxFPS) {
        this.minVideoWidth = minVideoWidth;
        this.minVideoHeight = minVideoHeight;
        this.maxVideoWidth = maxVideoWidth;
        this.maxVideoHeight = maxVideoHeight;
        this.minFPS = minFPS;
        this.maxFPS = maxFPS;
    }

    public int getMinVideoWidth() {
        return minVideoWidth;
    }

    public int getMinVideoHeight() {
        return minVideoHeight;
    }

    public int getMaxVideoWidth() {
        return maxVideoWidth;
    }

    public int getMaxVideoHeight() {
        return maxVideoHeight;
    }

    public int getMinFPS() {
        return minFPS;
    }

    public int getMaxFPS() {
        return maxFPS;
    }

    public static class Builder {
        private int minVideoWidth;
        private int minVideoHeight;
        private int maxVideoWidth = VideoConstraints.MAX_VIDEO_WIDTH;
        private int maxVideoHeight = VideoConstraints.MAX_VIDEO_HEIGHT;
        private int minFPS = 1;
        private int maxFPS = 30;

        public Builder() { }

        // Option 1: Use individual parameters to specify dimensions
        public Builder setMinVideoDimensions(int minVideoWidth, int minVideoHeight) {
            this.minVideoWidth = minVideoWidth;
            this.minVideoHeight = minVideoHeight;
            return this;
        }

        public Builder setMaxVideoDimensions(int maxVideoWidth, int maxVideoHeight) {
            this.maxVideoWidth = maxVideoWidth;
            this.maxVideoHeight = maxVideoHeight;
            return this;
        }

        // Option 2: Use Point to describe video dimensions
        public Builder setMinVideoDimensions(Point minVideoDimensions) {
            this.minVideoWidth = minVideoDimensions.x;
            this.minVideoHeight = minVideoDimensions.y;
            return this;
        }

        public Builder setMaxVideoDimensions(Point maxVideoDimensions) {
            this.maxVideoWidth = maxVideoDimensions.x;
            this.maxVideoHeight = maxVideoDimensions.y;
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
            return new VideoConstraints(minVideoWidth, minVideoHeight, maxVideoWidth, maxVideoHeight, minFPS, maxFPS);
        }
    }

}
