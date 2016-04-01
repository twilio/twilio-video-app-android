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

    // HD Standard 1080P (1440 x 1080) resolution
    public static final int HD_S1080P_VIDEO_WIDTH = 1440;
    public static final int HD_S1080P_VIDEO_HEIGHT = 1080;
    public static final VideoDimensions HD_S1080P_VIDEO_DIMENSIONS = new VideoDimensions(HD_S1080P_VIDEO_WIDTH, HD_S1080P_VIDEO_HEIGHT);

    // HD Widescreen 1080P (1920 x 1080) resolution
    public static final int HD_1080P_VIDEO_WIDTH = 1920;
    public static final int HD_1080P_VIDEO_HEIGHT = 1080;
    public static final VideoDimensions HD_1080P_VIDEO_DIMENSIONS = new VideoDimensions(HD_1080P_VIDEO_WIDTH, HD_1080P_VIDEO_HEIGHT);

    // Minimum resolution
    public static final int MIN_VIDEO_WIDTH = CIF_VIDEO_WIDTH;
    public static final int MIN_VIDEO_HEIGHT = CIF_VIDEO_HEIGHT;
    public static final VideoDimensions MIN_VIDEO_DIMENSIONS = new VideoDimensions(MIN_VIDEO_WIDTH, MIN_VIDEO_HEIGHT);

    // Maximum resolution
    public static final int MAX_VIDEO_WIDTH = HD_960P_VIDEO_WIDTH;
    public static final int MAX_VIDEO_HEIGHT = 1280;
    public static final VideoDimensions MAX_VIDEO_DIMENSIONS = new VideoDimensions(MAX_VIDEO_WIDTH, MAX_VIDEO_HEIGHT);

    // Fps setting options
    public static final int MIN_FRAME_RATE = 1;
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
    public static final int MAX_FRAME_RATE = FRAME_RATE_30;

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
                throw new IllegalStateException("MinFPS " + minFps + " is greater than MaxFps " + maxFps);
            }
            if(minFps < 0) {
                throw new IllegalStateException("MinFps is less than 0");
            }
            if(maxFps < 0) {
                throw new IllegalStateException("MaxFps is less than 0");
            }
            if(minFps > maxFps) {
                throw new IllegalStateException("Minfps is greater than maxFps");
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
