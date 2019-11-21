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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/** Represents a video frame provided by a {@link CameraCapturer}. */
public class VideoFrame {

    public enum RotationAngle {
        ROTATION_0(0),
        ROTATION_90(90),
        ROTATION_180(180),
        ROTATION_270(270);

        private final int rotation;

        RotationAngle(int rotation) {
            this.rotation = rotation;
        }

        public int getValue() {
            return this.rotation;
        }

        @NonNull
        public static RotationAngle fromInt(int rotation) {
            if (rotation == 0) {
                return ROTATION_0;
            } else if (rotation == 90) {
                return ROTATION_90;
            } else if (rotation == 180) {
                return ROTATION_180;
            } else if (rotation == 270) {
                return ROTATION_270;
            } else {
                throw new IllegalArgumentException(
                        "Orientation value must be 0, 90, 180 or 270: " + rotation);
            }
        }
    }

    final tvi.webrtc.VideoFrame webRtcVideoFrame;

    /** The bytes of a frame. */
    public final byte[] imageBuffer;
    /** The size of a frame. */
    @NonNull public final VideoDimensions dimensions;
    /** The orientation of a frame in degrees (must be multiple of 90). */
    @NonNull public final RotationAngle orientation;
    /** The time in nanoseconds at which this frame was captured. */
    public final long timestamp;

    public VideoFrame(
            byte[] imageBuffer,
            @NonNull VideoDimensions dimensions,
            @NonNull RotationAngle orientation,
            long timestamp) {
        this(imageBuffer, null, dimensions, orientation, timestamp);
    }

    /*
     * This constructor is currently only used by Twilio capturers because they wrap
     * WebRTC capturers which have access to tvi.webrtc.VideoFrame.
     */
    VideoFrame(
            tvi.webrtc.VideoFrame webRtcVideoFrame,
            @NonNull VideoDimensions dimensions,
            @NonNull RotationAngle orientation) {
        this(null, webRtcVideoFrame, dimensions, orientation, webRtcVideoFrame.getTimestampNs());
    }

    private VideoFrame(
            @Nullable byte[] imageBuffer,
            @Nullable tvi.webrtc.VideoFrame webRtcVideoFrame,
            @NonNull VideoDimensions dimensions,
            @NonNull RotationAngle orientation,
            long timestamp) {
        this.imageBuffer = imageBuffer;
        this.webRtcVideoFrame = webRtcVideoFrame;
        this.dimensions = dimensions;
        this.orientation = orientation;
        this.timestamp = timestamp;
    }
}
