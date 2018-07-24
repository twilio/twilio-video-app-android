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

public class VideoDimensions {
    public static final int CIF_VIDEO_WIDTH = 352;
    public static final int CIF_VIDEO_HEIGHT = 288;
    /** CIF (352 x 288) resolution in 1.22:1 aspect ratio */
    public static final VideoDimensions CIF_VIDEO_DIMENSIONS =
            new VideoDimensions(CIF_VIDEO_WIDTH, CIF_VIDEO_HEIGHT);

    public static final int VGA_VIDEO_WIDTH = 640;
    public static final int VGA_VIDEO_HEIGHT = 480;
    /** VGA (640 x 480) resolution in 4:3 aspect ratio */
    public static final VideoDimensions VGA_VIDEO_DIMENSIONS =
            new VideoDimensions(VGA_VIDEO_WIDTH, VGA_VIDEO_HEIGHT);

    public static final int WVGA_VIDEO_WIDTH = 800;
    public static final int WVGA_VIDEO_HEIGHT = 480;
    /** WVGA (800 x 480) resolution */
    public static final VideoDimensions WVGA_VIDEO_DIMENSIONS =
            new VideoDimensions(WVGA_VIDEO_WIDTH, WVGA_VIDEO_HEIGHT);

    public static final int HD_540P_VIDEO_WIDTH = 960;
    public static final int HD_540P_VIDEO_HEIGHT = 540;
    /** HD 540P (960 x 540) resolution */
    public static final VideoDimensions HD_540P_VIDEO_DIMENSIONS =
            new VideoDimensions(HD_540P_VIDEO_WIDTH, HD_540P_VIDEO_HEIGHT);

    public static final int HD_720P_VIDEO_WIDTH = 1280;
    public static final int HD_720P_VIDEO_HEIGHT = 720;
    /** HD 720P (1280 x 720) resolution */
    public static final VideoDimensions HD_720P_VIDEO_DIMENSIONS =
            new VideoDimensions(HD_720P_VIDEO_WIDTH, HD_720P_VIDEO_HEIGHT);

    public static final int HD_960P_VIDEO_WIDTH = 1280;
    public static final int HD_960P_VIDEO_HEIGHT = 960;
    /** HD 960P (1280 x 960) resolution */
    public static final VideoDimensions HD_960P_VIDEO_DIMENSIONS =
            new VideoDimensions(HD_960P_VIDEO_WIDTH, HD_960P_VIDEO_HEIGHT);

    public static final int HD_S1080P_VIDEO_WIDTH = 1440;
    public static final int HD_S1080P_VIDEO_HEIGHT = 1080;
    /** HD Standard 1080P (1440 x 1080) resolution */
    public static final VideoDimensions HD_S1080P_VIDEO_DIMENSIONS =
            new VideoDimensions(HD_S1080P_VIDEO_WIDTH, HD_S1080P_VIDEO_HEIGHT);

    public static final int HD_1080P_VIDEO_WIDTH = 1920;
    public static final int HD_1080P_VIDEO_HEIGHT = 1080;
    /** HD Widescreen 1080P (1920 x 1080) resolution */
    public static final VideoDimensions HD_1080P_VIDEO_DIMENSIONS =
            new VideoDimensions(HD_1080P_VIDEO_WIDTH, HD_1080P_VIDEO_HEIGHT);

    public final int width;
    public final int height;

    public VideoDimensions(int width, int height) {
        if (width < 0) {
            throw new IllegalStateException("Width must not be less than 0");
        }
        if (height < 0) {
            throw new IllegalStateException("Height must not be less than 0");
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoDimensions that = (VideoDimensions) o;

        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(width) + "x" + String.valueOf(height);
    }
}
