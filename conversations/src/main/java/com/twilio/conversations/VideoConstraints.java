package com.twilio.conversations;

public class VideoConstraints {

    // CIF (352 x 288) resolution in 1.22:1 aspect ratio
    public static final int CIF_VIDEO_WIDTH = 352;
    public static final int CIF_VIDEO_HEIGHT = 288;
    public static final VideoDimensions CIF_VIDEO_DIMENSIONS = new VideoDimensions(CIF_VIDEO_WIDTH, CIF_VIDEO_HEIGHT);

    // VGA (640 x 480) resolution in 4:3 aspect ratio
    public static final int VGA_VIDEO_WIDTH = 640;
    public static final int VGA_VIDEO_HEIGHT = 480;
    public static final VideoDimensions VGA_VIDEO_DIMENSIONS = new VideoDimensions(VGA_VIDEO_WIDTH, VGA_VIDEO_HEIGHT);

    // WVGA (800 x 480) resolution
    public static final int WVGA_VIDEO_WIDTH = 800;
    public static final int WVGA_VIDEO_HEIGHT = 480;
    public static final VideoDimensions WVGA_VIDEO_DIMENSIONS = new VideoDimensions(WVGA_VIDEO_WIDTH, WVGA_VIDEO_HEIGHT);

    // HD 540P (960 x 540) resolution
    public static final int HD_540P_VIDEO_WIDTH = 960;
    public static final int HD_540P_VIDEO_HEIGHT = 540;
    public static final VideoDimensions HD_540P_VIDEO_DIMENSIONS = new VideoDimensions(HD_540P_VIDEO_WIDTH, HD_540P_VIDEO_HEIGHT);

    // HD 720P (1280 x 720) resolution
    public static final int HD_720P_VIDEO_WIDTH = 1280;
    public static final int HD_720P_VIDEO_HEIGHT = 720;
    public static final VideoDimensions HD_720P_VIDEO_DIMENSIONS = new VideoDimensions(HD_720P_VIDEO_WIDTH, HD_720P_VIDEO_HEIGHT);

    // HD 960P (1280 x 960) resolution
    public static final int HD_960P_VIDEO_WIDTH = 1280;
    public static final int HD_960P_VIDEO_HEIGHT = 960;
    public static final VideoDimensions HD_960P_VIDEO_DIMENSIONS = new VideoDimensions(HD_960P_VIDEO_WIDTH, HD_960P_VIDEO_HEIGHT);

    // Minimum resolution
    public static final int MIN_VIDEO_WIDTH = CIF_VIDEO_WIDTH;
    public static final int MIN_VIDEO_HEIGHT = CIF_VIDEO_HEIGHT;
    public static final VideoDimensions MIN_VIDEO_DIMENSIONS = new VideoDimensions(MIN_VIDEO_WIDTH, MIN_VIDEO_HEIGHT);

    // Maximum resolution
    public static final int MAX_VIDEO_WIDTH = HD_960P_VIDEO_WIDTH;
    public static final int MAX_VIDEO_HEIGHT = 1280;
    public static final VideoDimensions MAX_VIDEO_DIMENSIONS = new VideoDimensions(MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT);

    // FPS setting options
    public static final int MIN_VIDEO_FPS = 1;
    public static final int BATTERY_SAVER_10_FPS = 10;
    public static final int BATTERY_EFFICIENT_15_FPS = 15;
    public static final int BATTERY_EFFICIENT_20_FPS = 20;
    public static final int CINEMATIC_24_FPS = 24;
    public static final int DEFAULT_FPS = 30;
    public static final int MAX_VIDEO_FPS = DEFAULT_FPS;

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
        private VideoDimensions minVideoDimensions = MIN_VIDEO_DIMENSIONS;
        private VideoDimensions maxVideoDimensions = MAX_VIDEO_DIMENSIONS;
        private int minFps = MIN_VIDEO_FPS;
        private int maxFps = MAX_VIDEO_FPS;

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
            return new VideoConstraints(this);
        }
    }

}
