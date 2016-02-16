package com.twilio.conversations;


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
     * A combintation of fit and fill. Will scale, fit, and crop accordingly to
     * internal visibility fraction.
     */
    ASPECT_BALANCED
}
