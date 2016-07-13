package com.twilio.conversations;

public class VideoDimensions {

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

    public final int width;
    public final int height;

    public VideoDimensions(int width, int height) {
        if(width < 0) {
            throw new IllegalStateException("Width must not be less than 0");
        }
        if(height < 0) {
            throw new IllegalStateException("Height must not be less than 0");
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return String.valueOf(width) + "x" + String.valueOf(height);
    }

}
