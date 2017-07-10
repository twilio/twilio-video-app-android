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
 * Specifies what type of video scaling that will be performed by a {@link VideoRenderer}.
 */
public enum VideoScaleType {
    /**
     * Video is scaled to fit size of view while maintaining aspect ratio. Black bars may appear.
     */
    ASPECT_FIT,

    /**
     * Video is scaled to fill entire view and preserve aspect ratio. This may cause cropping.
     */
    ASPECT_FILL,

    /**
     * A combination of fit and fill. Will scale, fit, and crop accordingly to
     * internal visibility fraction.
     */
    ASPECT_BALANCED;

    static VideoScaleType fromInt(int scaleTypeInt) {
        if (scaleTypeInt == VideoScaleType.ASPECT_FIT.ordinal()) {
            return VideoScaleType.ASPECT_FIT;
        } else if (scaleTypeInt == VideoScaleType.ASPECT_FILL.ordinal()) {
            return VideoScaleType.ASPECT_FILL;
        } else if (scaleTypeInt == VideoScaleType.ASPECT_BALANCED.ordinal()) {
            return VideoScaleType.ASPECT_BALANCED;
        } else {
            return VideoScaleType.ASPECT_FIT;
        }
    }
}
