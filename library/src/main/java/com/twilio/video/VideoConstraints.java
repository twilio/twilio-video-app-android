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

/**
 * Use video constraints to apply to a {@link LocalVideoTrack}.
 *
 * <p>The default video constraints is a 4:3 aspect ratio with a maximum resolution of
 * 640x480 and a maximum of 30 fps.</p>
 *
 * <p><b>Note</b>: {@link VideoConstraints} is used to resolve the capture format, but the actual
 * video sent to Participants may be downscaled temporally or spatially in response to network
 * and device conditions.</p>
 */
public class VideoConstraints {
    /** Battery saving 10 fps video. */
    public static final int FPS_10 = 10;
    /** Battery saving 15 fps video. */
    public static final int FPS_15 = 15;
    /** Battery efficient 20 fps video. */
    public static final int FPS_20 = 20;
    /** Cinematic 24 fps video. */
    public static final int FPS_24 = 24;
    /** Smooth 30 fps video. */
    public static final int FPS_30 = 30;

    /** Pre-defined aspect ratio 4:3. */
    public static final AspectRatio ASPECT_RATIO_4_3 = new AspectRatio(4, 3);

    /** Pre-defined aspect ratio 16:9. */
    public static final AspectRatio ASPECT_RATIO_16_9 = new AspectRatio(16, 9);

    /** Pre-defined aspect ratio 11:9. */
    public static final AspectRatio ASPECT_RATIO_11_9 = new AspectRatio(11, 9);

    private final VideoDimensions minVideoDimensions;
    private final VideoDimensions maxVideoDimensions;
    private final int minFps;
    private final int maxFps;

    private final AspectRatio aspectRatio;

    private VideoConstraints(Builder builder) {
        this.minVideoDimensions = builder.minVideoDimensions;
        this.maxVideoDimensions = builder.maxVideoDimensions;
        this.minFps = builder.minFps;
        this.maxFps = builder.maxFps;
        this.aspectRatio = builder.aspectRatio;
    }

    /**
     * The minimum video size allowed.
     */
    public VideoDimensions getMinVideoDimensions() {
        return minVideoDimensions;
    }

    /**
     * The maximum video size allowed.
     */
    public VideoDimensions getMaxVideoDimensions() {
        return maxVideoDimensions;
    }

    /**
     * The minimum frames per second allowed.
     */
    public int getMinFps() {
        return minFps;
    }

    /**
     * The maximum frames per second allowed.
     */
    public int getMaxFps() {
        return maxFps;
    }

    /**
     * The aspect ratio.
     */
    public AspectRatio getAspectRatio() {
        return aspectRatio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoConstraints that = (VideoConstraints) o;

        if (minFps != that.minFps) return false;
        if (maxFps != that.maxFps) return false;
        if (!minVideoDimensions.equals(that.minVideoDimensions)) return false;
        if (!maxVideoDimensions.equals(that.maxVideoDimensions)) return false;
        return aspectRatio.equals(that.aspectRatio);

    }

    @Override
    public int hashCode() {
        int result = minVideoDimensions.hashCode();
        result = 31 * result + maxVideoDimensions.hashCode();
        result = 31 * result + minFps;
        result = 31 * result + maxFps;
        result = 31 * result + aspectRatio.hashCode();
        return result;
    }

    public static class Builder {
        private VideoDimensions minVideoDimensions = new VideoDimensions(0,0);
        private VideoDimensions maxVideoDimensions = new VideoDimensions(0,0);
        private int minFps = 0;
        private int maxFps = 0;
        private AspectRatio aspectRatio = new AspectRatio(0,0);

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

        public Builder aspectRatio(AspectRatio aspectRatio) {
            this.aspectRatio = aspectRatio;
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
                throw new IllegalStateException("MinFps " + minFps + " is greater than maxFps " +
                        maxFps);
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
                throw new IllegalStateException("Min video dimensions width " +
                        minVideoDimensions.width + " is greater than max video dimensions width " +
                        maxVideoDimensions.width);
            }
            if(minVideoDimensions.height > maxVideoDimensions.height) {
                throw new IllegalStateException("Min video dimensions height " +
                        minVideoDimensions.height+ " is greater than max video dimensions height " +
                        maxVideoDimensions.height);
            }
            if (aspectRatio.numerator < 0) {
                throw new IllegalStateException("aspectRatio numerator is less than 0");
            }
            if (aspectRatio.denominator < 0) {
                throw new IllegalStateException("aspectRatio denominator is less than 0");
            }
            return new VideoConstraints(this);
        }
    }
}
