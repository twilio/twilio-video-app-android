package com.twilio.video;


/**
 * Specifies what type of video scaling that will be performed by a {@link VideoRenderer}
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
